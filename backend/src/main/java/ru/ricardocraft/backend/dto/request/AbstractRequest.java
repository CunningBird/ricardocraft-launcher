package ru.ricardocraft.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.ricardocraft.backend.dto.request.auth.*;
import ru.ricardocraft.backend.dto.request.cabinet.AssetUploadInfoRequest;
import ru.ricardocraft.backend.dto.request.cabinet.GetAssetUploadInfoRequest;
import ru.ricardocraft.backend.dto.request.management.FeaturesRequest;
import ru.ricardocraft.backend.dto.request.management.GetConnectUUIDRequest;
import ru.ricardocraft.backend.dto.request.management.GetPublicKeyRequest;
import ru.ricardocraft.backend.dto.request.profile.BatchProfileByUsername;
import ru.ricardocraft.backend.dto.request.profile.ProfileByUUIDRequest;
import ru.ricardocraft.backend.dto.request.profile.ProfileByUsername;
import ru.ricardocraft.backend.dto.request.secure.GetSecureLevelInfoRequest;
import ru.ricardocraft.backend.dto.request.secure.HardwareReportRequest;
import ru.ricardocraft.backend.dto.request.secure.SecurityReportRequest;
import ru.ricardocraft.backend.dto.request.secure.VerifySecureLevelKeyRequest;
import ru.ricardocraft.backend.dto.request.update.LauncherRequest;
import ru.ricardocraft.backend.dto.request.update.UpdateRequest;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        defaultImpl = UnknownRequest.class
)
@JsonSubTypes({
        // Auth
        @JsonSubTypes.Type(value = AdditionalDataRequest.class, name = "additionalData"),
        @JsonSubTypes.Type(value = AuthRequest.class, name = "auth"),
        @JsonSubTypes.Type(value = CheckServerRequest.class, name = "checkServer"),
        @JsonSubTypes.Type(value = CurrentUserRequest.class, name = "currentUser"),
        @JsonSubTypes.Type(value = ExitRequest.class, name = "exit"),
        @JsonSubTypes.Type(value = FetchClientProfileKeyRequest.class, name = "clientProfileKey"),
        @JsonSubTypes.Type(value = GetAvailabilityAuthRequest.class, name = "getAvailabilityAuth"),
        @JsonSubTypes.Type(value = JoinServerRequest.class, name = "joinServer"),
        @JsonSubTypes.Type(value = ProfilesRequest.class, name = "profiles"),
        @JsonSubTypes.Type(value = RefreshTokenRequest.class, name = "refreshToken"),
        @JsonSubTypes.Type(value = RestoreRequest.class, name = "restore"),
        @JsonSubTypes.Type(value = SetProfileRequest.class, name = "setProfile"),

        // Update
        @JsonSubTypes.Type(value = LauncherRequest.class, name = "launcher"),
        @JsonSubTypes.Type(value = UpdateRequest.class, name = "update"),

        // Profile
        @JsonSubTypes.Type(value = BatchProfileByUsername.class, name = "batchProfileByUsername"),
        @JsonSubTypes.Type(value = ProfileByUsername.class, name = "profileByUsername"),
        @JsonSubTypes.Type(value = ProfileByUUIDRequest.class, name = "profileByUUID"),

        // Secure
        @JsonSubTypes.Type(value = GetSecureLevelInfoRequest.class, name = "getSecureLevelInfo"),
        @JsonSubTypes.Type(value = HardwareReportRequest.class, name = "hardwareReport"),
        @JsonSubTypes.Type(value = SecurityReportRequest.class, name = "securityReport"),
        @JsonSubTypes.Type(value = VerifySecureLevelKeyRequest.class, name = "verifySecureLevelKey"),

        // Management
        @JsonSubTypes.Type(value = FeaturesRequest.class, name = "features"),
        @JsonSubTypes.Type(value = GetConnectUUIDRequest.class, name = "getConnectUUID"),
        @JsonSubTypes.Type(value = GetPublicKeyRequest.class, name = "getPublicKey"),

        // Cabinet
        @JsonSubTypes.Type(value = AssetUploadInfoRequest.class, name = "assetUploadInfo"),
        @JsonSubTypes.Type(value = GetAssetUploadInfoRequest.class, name = "getAssetUploadUrl"),
})
public abstract class AbstractRequest {
    public UUID requestUUID;
    public transient UUID connectUUID;
    public transient String ip;

    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ;
    }

    public enum ThreadSafeStatus {
        NONE, READ, READ_WRITE
    }
}
