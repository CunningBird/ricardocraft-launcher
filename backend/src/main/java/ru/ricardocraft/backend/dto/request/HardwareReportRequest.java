package ru.ricardocraft.backend.dto.request;

import ru.ricardocraft.backend.dto.response.secure.HardwareReportResponse;

import java.util.Base64;

public class HardwareReportRequest extends Request<HardwareReportResponse> {
    public HardwareInfo hardware;

    @Override
    public String getType() {
        return "hardwareReport";
    }

    public static class HardwareInfo {
        public int bitness;
        public long totalMemory;
        public int logicalProcessors;
        public int physicalProcessors;
        public long processorMaxFreq;
        public boolean battery;
        public String hwDiskId;
        public byte[] displayId;
        public String baseboardSerialNumber;
        public String graphicCard;

        @Override
        public String toString() {
            return "HardwareInfo{" +
                    "bitness=" + bitness +
                    ", totalMemory=" + totalMemory +
                    ", logicalProcessors=" + logicalProcessors +
                    ", physicalProcessors=" + physicalProcessors +
                    ", processorMaxFreq=" + processorMaxFreq +
                    ", battery=" + battery +
                    ", hwDiskId='" + hwDiskId + '\'' +
                    ", displayId=" + (displayId == null ? null : new String(Base64.getEncoder().encode(displayId))) +
                    ", baseboardSerialNumber='" + baseboardSerialNumber + '\'' +
                    ", graphicCard='" + graphicCard + '\'' +
                    '}';
        }
    }
}
