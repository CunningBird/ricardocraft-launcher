package ru.ricardocraft.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.ricardocraft.backend.dto.response.auth.*;
import ru.ricardocraft.backend.dto.response.cabinet.AssetUploadInfoResponse;
import ru.ricardocraft.backend.dto.response.cabinet.GetAssetUploadInfoResponse;
import ru.ricardocraft.backend.dto.response.management.FeaturesResponse;
import ru.ricardocraft.backend.dto.response.management.GetConnectUUIDResponse;
import ru.ricardocraft.backend.dto.response.management.GetPublicKeyResponse;
import ru.ricardocraft.backend.dto.response.profile.BatchProfileByUsername;
import ru.ricardocraft.backend.dto.response.profile.ProfileByUUIDResponse;
import ru.ricardocraft.backend.dto.response.profile.ProfileByUsername;
import ru.ricardocraft.backend.dto.response.secure.GetSecureLevelInfoResponse;
import ru.ricardocraft.backend.dto.response.secure.HardwareReportResponse;
import ru.ricardocraft.backend.dto.response.secure.SecurityReportResponse;
import ru.ricardocraft.backend.dto.response.secure.VerifySecureLevelKeyResponse;
import ru.ricardocraft.backend.dto.response.update.LauncherResponse;
import ru.ricardocraft.backend.dto.response.update.UpdateResponse;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        defaultImpl = UnknownResponse.class
)
@JsonSubTypes({
        // Auth
        @JsonSubTypes.Type(value = AdditionalDataResponse.class, name = "additionalData"),
        @JsonSubTypes.Type(value = AuthResponse.class, name = "auth"),
        @JsonSubTypes.Type(value = CheckServerResponse.class, name = "checkServer"),
        @JsonSubTypes.Type(value = CurrentUserResponse.class, name = "currentUser"),
        @JsonSubTypes.Type(value = ExitResponse.class, name = "exit"),
        @JsonSubTypes.Type(value = FetchClientProfileKeyResponse.class, name = "clientProfileKey"),
        @JsonSubTypes.Type(value = GetAvailabilityAuthResponse.class, name = "getAvailabilityAuth"),
        @JsonSubTypes.Type(value = JoinServerResponse.class, name = "joinServer"),
        @JsonSubTypes.Type(value = ProfilesResponse.class, name = "profiles"),
        @JsonSubTypes.Type(value = RefreshTokenResponse.class, name = "refreshToken"),
        @JsonSubTypes.Type(value = RestoreResponse.class, name = "restore"),
        @JsonSubTypes.Type(value = SetProfileResponse.class, name = "setProfile"),

        // Update
        @JsonSubTypes.Type(value = LauncherResponse.class, name = "launcher"),
        @JsonSubTypes.Type(value = UpdateResponse.class, name = "update"),

        // Profile
        @JsonSubTypes.Type(value = BatchProfileByUsername.class, name = "batchProfileByUsername"),
        @JsonSubTypes.Type(value = ProfileByUsername.class, name = "profileByUsername"),
        @JsonSubTypes.Type(value = ProfileByUUIDResponse.class, name = "profileByUUID"),

        // Secure
        @JsonSubTypes.Type(value = GetSecureLevelInfoResponse.class, name = "getSecureLevelInfo"),
        @JsonSubTypes.Type(value = HardwareReportResponse.class, name = "hardwareReport"),
        @JsonSubTypes.Type(value = SecurityReportResponse.class, name = "securityReport"),
        @JsonSubTypes.Type(value = VerifySecureLevelKeyResponse.class, name = "verifySecureLevelKey"),

        // Management
        @JsonSubTypes.Type(value = FeaturesResponse.class, name = "features"),
        @JsonSubTypes.Type(value = GetConnectUUIDResponse.class, name = "getConnectUUID"),
        @JsonSubTypes.Type(value = GetPublicKeyResponse.class, name = "getPublicKey"),

        // Cabinet
        @JsonSubTypes.Type(value = AssetUploadInfoResponse.class, name = "assetUploadInfo"),
        @JsonSubTypes.Type(value = GetAssetUploadInfoResponse.class, name = "getAssetUploadUrl"),
})
public abstract class SimpleResponse {
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
