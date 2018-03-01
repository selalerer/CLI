package com.checkmarx.cxconsole.clients.sast.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

import static io.vertx.core.json.Json.mapper;

/**
 * Created by nirli on 01/03/2018.
 */
public class ScanSettingDTODeserializer extends StdDeserializer<ScanSettingDTO> {

    public ScanSettingDTODeserializer() {
        this(null);
    }

    public ScanSettingDTODeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ScanSettingDTO deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

        JsonNode ScanSettingDTONode = jp.getCodec().readTree(jp);
        ScanSettingDTO scanSettingDTO = new ScanSettingDTO();
        scanSettingDTO.setProjectId(ScanSettingDTONode.get("project").get("id").intValue());
        scanSettingDTO.setPresetId(ScanSettingDTONode.get("preset").get("id").intValue());
        scanSettingDTO.setEngineConfigurationId(ScanSettingDTONode.get("engineConfiguration").get("id").intValue());
        scanSettingDTO.setPostScanActionId(ScanSettingDTONode.get("postScanAction").intValue());

        ScanSettingDTO.EmailNotificationsDTO emailNotificationsDTO = mapper.convertValue(ScanSettingDTONode.get("emailNotifications"), ScanSettingDTO.EmailNotificationsDTO.class);
        scanSettingDTO.setEmailNotifications(emailNotificationsDTO);

        return scanSettingDTO;
    }
}
