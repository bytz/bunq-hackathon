package org.bunq.spending;

import com.bunq.sdk.context.ApiContext;
import com.bunq.sdk.context.ApiEnvironmentType;
import com.bunq.sdk.context.BunqContext;
import com.bunq.sdk.model.generated.endpoint.PaymentApiObject;
import com.bunq.sdk.model.generated.object.AmountObject;
import com.bunq.sdk.model.generated.object.PointerObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class MainTest {
    @Test
    @Disabled
    void testMain() {
        ApiContext apiContext = ApiContext.create(
            ApiEnvironmentType.SANDBOX,
            System.getenv("BUNQ_SANDBOX_API_KEY"),
            System.getenv("USER")
        );
        apiContext.save();
        BunqContext.loadApiContext(apiContext); //load the API context to use in your app

        PaymentApiObject.create(new AmountObject("1","EUR"), new PointerObject("EMAIL", "sugardaddy@bunq.com","Sugar Daddy"), "coffee");
        Main.main(new String[]{});
    }
}
