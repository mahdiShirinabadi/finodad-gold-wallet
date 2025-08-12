package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.enumaration.ProfileStatusEnum;
import com.melli.wallet.domain.master.entity.ChannelBlockEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.SettingGeneralEntity;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.SecurityOperationService;
import com.melli.wallet.service.repository.*;
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
public class SecurityOperationServiceImplementation implements SecurityOperationService {

    private static final String INVALID_SIGN = "invalid sign";

    private final ChannelBlockRepositoryService channelBlockRepositoryService;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final ChannelRepositoryService channelRepositoryService;

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
        SettingGeneralEntity setting = settingGeneralRepositoryService.getSetting(SettingGeneralRepositoryService.MAX_WRONG_PASSWORD_FOR_PROFILE);
        if (setting != null) {
            log.info("setting object for {} is not null and set value {} to maxFail", SettingGeneralRepositoryService.MAX_WRONG_PASSWORD_FOR_PROFILE, setting.getValue());
            maxFail = setting.getValue();
        }
        return Long.parseLong(maxFail);
    }

    public void resetFailLoginCount(ChannelEntity channelEntity) {
        ChannelBlockEntity profileBlock = channelBlockRepositoryService.findByProfile(channelEntity);
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
        channelBlockRepositoryService.save(profileBlock);
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
        ChannelBlockEntity currentProfileBlock = channelBlockRepositoryService.findByProfile(channelEntity);
        if (currentProfileBlock == null) {
            currentProfileBlock = new ChannelBlockEntity();
            currentProfileBlock.setCreatedAt(new Date());
            currentProfileBlock.setCreatedBy("System");
            currentProfileBlock.setStartBlockDate(new Date());
            currentProfileBlock.setCountFail(1);
            currentProfileBlock.setChannelEntity(channelEntity);
            channelBlockRepositoryService.save(currentProfileBlock);
        } else {
            currentProfileBlock.setStartBlockDate(new Date());
            if (currentProfileBlock.getCountFail() <= getMaxFail()){
                currentProfileBlock.setCountFail(currentProfileBlock.getCountFail() + 1);

                if(currentProfileBlock.getCountFail() == getMaxFail()){
                    channelEntity.setStatus(ProfileStatusEnum.BLOCK.getText());
                    channelEntity.setUpdatedAt(new Date());
                    channelRepositoryService.save(channelEntity);
                }
            }
            channelBlockRepositoryService.save(currentProfileBlock);
        }
    }

    public boolean isBlock(ChannelEntity channelEntity) {
        ChannelBlockEntity currentUserBlock = channelBlockRepositoryService.findByProfile(channelEntity);
        if(currentUserBlock == null){
            return false;
        }
        return Objects.equals(currentUserBlock.getChannelEntity().getStatus(), ProfileStatusEnum.BLOCK.getText());
    }

    @Override
    public void checkSign(ChannelEntity channelEntity, String sign, String data) throws InternalServiceException {
        log.info("start check sign for channel ({}), sign ({})...", channelEntity.getUsername(), sign);
        // channel not use sign
        if (channelEntity.getSign() == ChannelRepositoryService.FALSE) {
            log.info("success check sign for channel ({}), set in data base no need for check sign !!!!, and we return true", channelEntity.getUsername());
            return;
        }
        // fetch public key from dataBase
        String publicKey = channelEntity.getPublicKey();
        if ((publicKey == null) || (publicKey.isEmpty())) {
            log.error("failed check sign for channel ({}), public key is empty !!!! ", channelEntity.getUsername());
            throw new InternalServiceException(INVALID_SIGN, StatusRepositoryService.INVALID_SIGN, HttpStatus.OK);
        }
        try {
            log.info("start verify signature for channel ({})...", channelEntity.getUsername());
            boolean resultSign = verifySignature(data.getBytes(), sign.getBytes(), publicKey);
            if (resultSign) {
                log.info("success verify signature for channel ({})...", channelEntity.getUsername());
                return;
            }
            log.error("failed verify signature for channel ({}), result is false", channelEntity.getUsername());
        } catch (Exception e) {
            log.error("failed verify signature for channel ({}), error is ({}) ", channelEntity.getUsername(), e.getMessage());
        }
        log.info("failed check sign for channel ({})", channelEntity.getUsername());
        throw new InternalServiceException(INVALID_SIGN, StatusRepositoryService.INVALID_SIGN, HttpStatus.OK);
    }
}
