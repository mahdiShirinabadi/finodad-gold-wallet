package com.melli.hub.service.impl;

import com.melli.hub.domain.enumaration.ProfileStatusEnum;
import com.melli.hub.domain.master.entity.ChannelBlockEntity;
import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.master.entity.SettingEntity;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Objects;

/**
 * Class Name: SecurityServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class SecurityServiceImplementation implements SecurityService {

    private static final String INVALID_SIGN = "invalid sign";

    private final ChannelBlockService channelBlockService;
    private final SettingService settingService;
    private final ChannelService channelService;

    @Value("${default.maxFailForWrongPassword}")
    private String maxFail;

    // check signed value
    private static boolean verifySignature(byte[] data, byte[] signature, String publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(getPublic(publicKey));
        sig.update(data);
        return sig.verify(org.bouncycastle.util.encoders.Base64.decode(signature));
    }

    private static PublicKey getPublic(String key) throws Exception {

        byte[] keyBytes = DatatypeConverter.parseBase64Binary(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private long getMaxFail() {
        SettingEntity setting = settingService.getSetting(SettingService.MAX_WRONG_PASSWORD_FOR_PROFILE);
        if (setting != null) {
            log.info("setting object for {} is not null and set value {} to maxFail", SettingService.MAX_WRONG_PASSWORD_FOR_PROFILE, setting.getValue());
            maxFail = setting.getValue();
        }
        return Long.parseLong(maxFail);
    }

    public void resetFailLoginCount(ChannelEntity channelEntity) {
        ChannelBlockEntity profileBlock = channelBlockService.findByProfile(channelEntity);
        if (profileBlock == null) {
            log.info("channelBlock is null for profile ==> {}", channelEntity.getUsername());
            return;
        }
        if (profileBlock.getCountFail() == 0) {
            log.info("channelBlock's fail count is {} for profile ==> {}", 0, channelEntity.getUsername());
            return;
        }
        profileBlock.setEndBlockDate(new Date());
        profileBlock.setCountFail(0);
        channelBlockService.save(profileBlock);
    }

    @Async
    public void increaseFailLogin(ChannelEntity channelEntity) {
        if(channelEntity != null){
            log.info("increase failed login count for nationalCode ({})", channelEntity.getUsername());
            increaseFailLoginCount(channelEntity);
        }else{
            log.error("increase failed login count for nationalCode is null");
        }
    }


    private synchronized void increaseFailLoginCount(ChannelEntity channelEntity) {
        ChannelBlockEntity currentProfileBlock = channelBlockService.findByProfile(channelEntity);
        if (currentProfileBlock == null) {
            currentProfileBlock = new ChannelBlockEntity();
            currentProfileBlock.setCreatedAt(new Date());
            currentProfileBlock.setCreatedBy("System");
            currentProfileBlock.setStartBlockDate(new Date());
            currentProfileBlock.setCountFail(1);
            currentProfileBlock.setChannelEntity(channelEntity);
            channelBlockService.save(currentProfileBlock);
        } else {
            currentProfileBlock.setStartBlockDate(new Date());
            if (currentProfileBlock.getCountFail() <= getMaxFail()){
                currentProfileBlock.setCountFail(currentProfileBlock.getCountFail() + 1);

                if(currentProfileBlock.getCountFail() == getMaxFail()){
                    channelEntity.setStatus(ProfileStatusEnum.BLOCK.getText());
                    channelEntity.setUpdatedAt(new Date());
                    channelService.save(channelEntity);
                }
            }
            channelBlockService.save(currentProfileBlock);
        }
    }

    public boolean isBlock(ChannelEntity channelEntity) {
        ChannelBlockEntity currentUserBlock = channelBlockService.findByProfile(channelEntity);
        if(currentUserBlock == null){
            return false;
        }
        return Objects.equals(currentUserBlock.getChannelEntity().getStatus(), ProfileStatusEnum.BLOCK.getText());
    }

    @Override
    public void checkSign(ChannelEntity profileEntity, String sign, String data) throws InternalServiceException {
        log.info("start check sign for channel ({}), sign ({})...", profileEntity.getUsername(), sign);
        // channel not use sign
        if (profileEntity.getSign() == ChannelService.FALSE) {
            log.info("success check sign for channel ({}), set in data base no need for check sign !!!!, and we return true", profileEntity.getUsername());
            return;
        }
        // fetch public key from dataBase
        String publicKey = profileEntity.getPublicKey();
        if ((publicKey == null) || (publicKey.isEmpty())) {
            log.error("failed check sign for channel ({}), public key is empty !!!! ", profileEntity.getUsername());
            throw new InternalServiceException(INVALID_SIGN, StatusService.INVALID_SIGN, HttpStatus.OK);
        }
        try {
            log.info("start verify signature for channel ({})...", profileEntity.getUsername());
            boolean resultSign = verifySignature(data.getBytes(), sign.getBytes(), publicKey);
            if (resultSign) {
                log.info("success verify signature for channel ({})...", profileEntity.getUsername());
                return;
            }
            log.error("failed verify signature for channel ({}), result is false", profileEntity.getUsername());
        } catch (Exception e) {
            log.error("failed verify signature for channel ({}), error is ({}) ", profileEntity.getUsername(), e.getMessage());
        }
        log.info("failed check sign for channel ({})", profileEntity.getUsername());
        throw new InternalServiceException(INVALID_SIGN, StatusService.INVALID_SIGN, HttpStatus.OK);
    }
}
