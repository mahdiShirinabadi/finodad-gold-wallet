# gRPC Client Example with Authentication

## 🔐 **Authentication Flow**

### **1. Login to get JWT Token**

```java
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import com.melli.wallet.grpc.*;

public class GrpcClientExample {
    
    private static final String GRPC_SERVER_HOST = "localhost";
    private static final int GRPC_SERVER_PORT = 9090;
    
    public static void main(String[] args) {
        // Create channel
        ManagedChannel channel = ManagedChannelBuilder.forAddress(GRPC_SERVER_HOST, GRPC_SERVER_PORT)
                .usePlaintext()
                .build();
        
        try {
            // 1. Login to get JWT token
            String jwtToken = login(channel, "username", "password");
            
            // 2. Use JWT token for authenticated calls
            getMerchant(channel, jwtToken, "USD");
            
        } finally {
            channel.shutdown();
        }
    }
    
    private static String login(ManagedChannel channel, String username, String password) {
        AuthServiceGrpc.AuthServiceBlockingStub authStub = AuthServiceGrpc.newBlockingStub(channel);
        
        LoginRequestGrpc request = LoginRequestGrpc.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();
        
        BaseResponseGrpc response = authStub.login(request);
        
        if (response.getSuccess()) {
            LoginResponseGrpc loginResponse = response.getLoginResponse();
            TokenObjectGrpc accessToken = loginResponse.getAccessTokenObject();
            return accessToken.getToken();
        } else {
            throw new RuntimeException("Login failed: " + response.getErrorDetail().getMessage());
        }
    }
    
    private static void getMerchant(ManagedChannel channel, String jwtToken, String currency) {
        // Create metadata with JWT token
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), 
                    "Bearer " + jwtToken);
        
        // Create stub with metadata interceptor
        MerchantServiceGrpc.MerchantServiceBlockingStub merchantStub = 
                MerchantServiceGrpc.newBlockingStub(channel)
                        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
        
        GetMerchantRequestGrpc request = GetMerchantRequestGrpc.newBuilder()
                .setCurrency(currency)
                .build();
        
        BaseResponseGrpc response = merchantStub.getMerchant(request);
        
        if (response.getSuccess()) {
            MerchantResponseGrpc merchantResponse = response.getMerchantResponseGrpc();
            System.out.println("Merchants found: " + merchantResponse.getMerchantObjectListCount());
        } else {
            System.out.println("Error: " + response.getErrorDetail().getMessage());
        }
    }
}
```

## 🛠️ **Using with Different Tools**

### **1. Using grpcurl**

```bash
# Login to get token
grpcurl -plaintext -d '{"username": "your_username", "password": "your_password"}' \
  localhost:9090 wallet.AuthService/Login

# Use token for authenticated calls
grpcurl -plaintext -H "authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"currency": "USD"}' localhost:9090 wallet.MerchantService/GetMerchant
```

### **2. Using BloomRPC**

1. **Connect to server**: `localhost:9090`
2. **For Login**: No headers needed
3. **For other calls**: Add header `authorization: Bearer YOUR_JWT_TOKEN`

### **3. Using Postman (gRPC)**

1. **Import proto files**
2. **Set server**: `localhost:9090`
3. **For Login**: No metadata
4. **For other calls**: Add metadata `authorization: Bearer YOUR_JWT_TOKEN`

## 🔒 **Security Features**

### **Authentication (JWT)**
- ✅ Token validation
- ✅ Expiration check
- ✅ Channel status verification
- ✅ Username extraction from token

### **Authorization (RBAC)**
- ✅ Role-based access control
- ✅ Resource-based permissions
- ✅ Channel-role mapping
- ✅ Method-level security

### **Error Handling**
- ✅ `UNAUTHENTICATED` for invalid/missing tokens
- ✅ `PERMISSION_DENIED` for insufficient permissions
- ✅ Detailed error messages
- ✅ Comprehensive logging

## 📋 **Required Resources Mapping**

| gRPC Method | Required Resource |
|-------------|-------------------|
| `wallet.AuthService/Login` | None (Public) |
| `wallet.AuthService/RefreshToken` | None (Public) |
| `wallet.AuthService/Logout` | None (Public) |
| `wallet.MerchantService/GetMerchant` | `MERCHANT_LIST` |
| `wallet.PurchaseService/GenerateBuyUuid` | `GENERATE_PURCHASE_UNIQUE_IDENTIFIER` |
| `wallet.PurchaseService/GenerateSellUuid` | `GENERATE_PURCHASE_UNIQUE_IDENTIFIER` |
| `wallet.PurchaseService/Inquiry` | `GENERATE_PURCHASE_UNIQUE_IDENTIFIER` |
| `wallet.PurchaseService/Buy` | `BUY` |
| `wallet.PurchaseService/BuyDirect` | `BUY_DIRECT` |
| `wallet.PurchaseService/Sell` | `SELL` |
| `wallet.WalletService/CreateWallet` | `WALLET_CREATE` |
| `wallet.WalletService/DeactivateWallet` | `WALLET_DEACTIVATE` |
| `wallet.WalletService/DeleteWallet` | `WALLET_DELETE` |
| `wallet.WalletService/GetWallet` | `WALLET_INFO` |
| `wallet.WalletService/ActivateWallet` | `WALLET_ACTIVE` |
| `wallet.LimitationService/GetLimitationValue` | `LIMITATION_LIST` |
| `wallet.LimitationService/GetLimitationList` | `LIMITATION_LIST` |

## 🚀 **Testing Steps**

1. **Start gRPC server**
2. **Login to get JWT token**
3. **Use token in subsequent calls**
4. **Verify authorization works correctly**
5. **Test with different roles/resources**

## ⚠️ **Important Notes**

- **JWT Secret**: Configure `jwt.secret` in `application.properties`
- **gRPC Port**: Default is `9090`, configurable via `grpc.server.port`
- **Token Expiration**: Tokens expire based on JWT configuration
- **Channel Status**: Only `ACTIVE` channels can authenticate
- **Role Assignment**: Channels must have roles assigned with appropriate resources
