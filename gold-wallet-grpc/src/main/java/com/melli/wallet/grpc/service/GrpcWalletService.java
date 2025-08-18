package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.CreateWalletRequestGrpc;
import com.melli.wallet.grpc.DeactivateWalletRequestGrpc;
import com.melli.wallet.grpc.WalletServiceGrpc;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.grpc.exception.GrpcErrorHandler;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.util.Utility;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;


/**
 * Class Name: GrpcWalletService
 * Author: Mahdi Shirinabadi
 * Date: 6/7/2025
 */
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcWalletService extends WalletServiceGrpc.WalletServiceImplBase {

    private final WalletOperationalService walletOperationalService;
    private final GrpcErrorHandler exceptionHandler;

    @PreAuthorize("hasAuthority(\""+ ResourceRepositoryService.WALLET_CREATE +"\")")
    @Override
    public void createWallet(CreateWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            String username = RequestContext.getUsername();
            String cleanMobile = Utility.cleanPhoneNumber(request.getMobile());
            log.info("start call create wallet in username ===> {}, mobile ===> {}, from ip ===> {}",
                    username, request.getMobile(), channelIp);

            ChannelEntity channelEntity = RequestContext.getChannelEntity();

            CreateWalletResponse response = walletOperationalService.createWallet(
                    channelEntity,
                    cleanMobile,
                    request.getNationalCode(),
                    WalletTypeRepositoryService.NORMAL_USER,
                    List.of(WalletAccountCurrencyRepositoryService.GOLD, WalletAccountCurrencyRepositoryService.RIAL),
                    List.of(WalletAccountTypeRepositoryService.NORMAL)
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setCreateWalletResponse(convertToGrpcCreateWalletResponse(response))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException ex) {
            exceptionHandler.handleException(ex, responseObserver, "WalletService/CreateWallet");
        }
    }

    @PreAuthorize("hasAuthority(\""+ ResourceRepositoryService.WALLET_DEACTIVATE +"\")")
    @Override
    public void deactivateWallet(DeactivateWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            log.info("start disable wallet with id ==> {}", request.getId());

            walletOperationalService.deactivateWallet(
                    RequestContext.getChannelEntity(),
                    request.getId(),
                    channelIp
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "WalletService/DeactivateWallet");
        }
    }

    @PreAuthorize("hasAuthority(\""+ ResourceRepositoryService.WALLET_DELETE +"\")")
    @Override
    public void deleteWallet(DeleteWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            log.info("start delete wallet with id ==> {}", request.getId());

            walletOperationalService.deleteWallet(
                    RequestContext.getChannelEntity(),
                    request.getId(),
                    channelIp
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "WalletService/DeleteWallet");
        }
    }

    @PreAuthorize("hasAuthority(\""+ ResourceRepositoryService.WALLET_INFO +"\")")
    @Override
    public void getWallet(GetWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            log.info("start get wallet with nationalCode ==> {}, from Ip ({})", request.getNationalCode(), channelIp);

            CreateWalletResponse response = walletOperationalService.get(
                    RequestContext.getChannelEntity(),
                    request.getNationalCode()
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setCreateWalletResponse(convertToGrpcCreateWalletResponse(response))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "WalletService/GetWallet");
        }
    }

    @PreAuthorize("hasAuthority(\""+ ResourceRepositoryService.WALLET_ACTIVE +"\")")
    @Override
    public void activateWallet(ActivateWalletRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String channelIp = RequestContext.getClientIp();
            log.info("start disable wallet with mobile ==> {}", request.getId());

            walletOperationalService.activateWallet(
                    RequestContext.getChannelEntity(),
                    request.getId(),
                    channelIp
            );

            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "WalletService/ActiveWallet");
        }
    }

    private CreateWalletResponseGrpc convertToGrpcCreateWalletResponse(CreateWalletResponse response) {
        // Adjust fields based on your actual CreateWalletResponse class
        CreateWalletResponseGrpc.Builder builder = CreateWalletResponseGrpc.newBuilder()
                .setWalletId(response.getWalletId() != null ? response.getWalletId() : "")
                .setMobile(response.getMobile() != null ? response.getMobile() : "")
                .setNationalCode(response.getNationalCode() != null ? response.getNationalCode() : "")
                .setStatus(response.getStatus() != null ? response.getStatus() : "")
                .setStatusDescription(response.getStatusDescription() != null ? response.getStatusDescription() : "");

        if (response.getWalletAccountObjectList() != null) {
            builder.addAllWalletAccountObjectList(
                    response.getWalletAccountObjectList().stream()
                            .map(this::convertToGrpcWalletAccountObject)
                            .toList()
            );
        }
        return builder.build();
    }

    private WalletAccountObjectGrpc convertToGrpcWalletAccountObject(WalletAccountObject walletAccount) {
        WalletAccountObjectGrpc.Builder builder = WalletAccountObjectGrpc.newBuilder();
        // Set account_number
        builder.setAccountNumber(walletAccount.getAccountNumber());
        // Set balance
        builder.setBalance(walletAccount.getBalance());
        // Set status
        builder.setStatus(walletAccount.getStatus());
        // Set status_description
        builder.setStatusDescription(walletAccount.getStatusDescription());
        // Set wallet_account_type_object
        WalletAccountTypeObjectGrpc.Builder typeBuilder = WalletAccountTypeObjectGrpc.newBuilder();
        typeBuilder.setId(walletAccount.getWalletAccountTypeObject().getId());
        typeBuilder.setName(walletAccount.getWalletAccountTypeObject().getName());
        builder.setWalletAccountTypeObject(typeBuilder.build());
        // Set wallet_account_currency_object
        WalletAccountCurrencyObjectGrpc.Builder currencyBuilder = WalletAccountCurrencyObjectGrpc.newBuilder();
        currencyBuilder.setId(walletAccount.getWalletAccountCurrencyObject().getId());
        currencyBuilder.setName(walletAccount.getWalletAccountCurrencyObject().getName());
        builder.setWalletAccountCurrencyObject(currencyBuilder.build());


        return builder.build();
    }

}
