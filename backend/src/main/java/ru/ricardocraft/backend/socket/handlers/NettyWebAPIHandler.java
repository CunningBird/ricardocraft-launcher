package ru.ricardocraft.backend.socket.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.socket.NettyConnectContext;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NettyWebAPIHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private transient final Logger logger = LogManager.getLogger(NettyWebAPIHandler.class);

    private static final TreeSet<SeverletPathPair> severletList = new TreeSet<>(Comparator.comparingInt((e) -> -e.key.length()));
    private static final DefaultFullHttpResponse ERROR_500 = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(IOHelper.encode("Internal Server Error 500")));

    static {
        ERROR_500.retain();
    }

    private final NettyConnectContext context;

    public NettyWebAPIHandler(NettyConnectContext context) {
        super();
        this.context = context;
    }

    public static void addNewServlet(String path, SimpleServletHandler callback) {
        SeverletPathPair pair = new SeverletPathPair("/webapi/".concat(path), callback);
        severletList.add(pair);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        boolean isNext = true;
        for (SeverletPathPair pair : severletList) {
            if (msg.uri().startsWith(pair.key)) {
                try {
                    pair.callback.handle(ctx, msg, context);
                } catch (Throwable e) {
                    logger.error("WebAPI Error", e);
                    ctx.writeAndFlush(ERROR_500, ctx.voidPromise());
                }
                isNext = false;
                break;
            }
        }
        if (isNext) {
            msg.retain();
            ctx.fireChannelRead(msg);
        }
    }

    @FunctionalInterface
    public interface SimpleServletHandler {
        void handle(ChannelHandlerContext ctx, FullHttpRequest msg, NettyConnectContext context) throws JsonProcessingException;

        default Map<String, String> getParamsFromUri(String uri) {
            int ind = uri.indexOf("?");
            if (ind <= 0) {
                return Map.of();
            }
            String sub = uri.substring(ind + 1);
            String[] result = sub.split("&");
            Map<String, String> map = new HashMap<>();
            for (String s : result) {
                String c = URLDecoder.decode(s, StandardCharsets.UTF_8);
                int index = c.indexOf("=");
                if (index <= 0) {
                    continue;
                }
                String key = c.substring(0, index);
                String value = c.substring(index + 1);
                map.put(key, value);
            }
            return map;
        }

        default FullHttpResponse simpleJsonResponse(HttpResponseStatus status, Object body, JacksonManager jacksonManager) throws JsonProcessingException {
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, status, body != null ? Unpooled.wrappedBuffer(IOHelper.encode(jacksonManager.getMapper().writeValueAsString(body))) : Unpooled.buffer());
            httpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
            return httpResponse;
        }

        default void sendHttpResponse(ChannelHandlerContext ctx, FullHttpResponse response) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static class SeverletPathPair {
        public final String key;
        public final SimpleServletHandler callback;

        public SeverletPathPair(String key, SimpleServletHandler callback) {
            this.key = key;
            this.callback = callback;
        }
    }
}
