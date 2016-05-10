package com.harpz.paypaltransaction;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    //Paypal Client id
    private static final String CONFIG_CLIENT_ID = "<PAYPAL CLIENT-ID>";


    // Set Paypal Environment
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;


    private static final int REQUEST_CODE_PAYMENT =  1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;


    //Make the object of the paypal for paypal service
    private static PayPalConfiguration paypalconfi =new PayPalConfiguration().environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
            .acceptCreditCards(true)

            //the need for future payment like merchant name privacy policy
            .merchantName("test")
            .merchantPrivacyPolicyUri(
            Uri.parse("https://www.example.com/privacy"))
            .merchantUserAgreementUri(
            Uri.parse("https://www.example.com/legal"));



    PayPalPayment thingtoBuy;

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, PayPalService.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalconfi);
        startService(intent);

        tv = (TextView) findViewById(R.id.text);

        Button btn = (Button) findViewById(R.id.btn);



        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               //future payment method call
                onFuturePaymentPressed(v);
            }
        });

        //button click paypal payment activity open
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thingtoBuy = new PayPalPayment(new BigDecimal("10"), "USD", "HeadSet", PayPalPayment.PAYMENT_INTENT_SALE);
                Intent intent = new Intent(MainActivity.this, PaymentActivity.class);

                intent.putExtra(PaymentActivity.EXTRA_PAYMENT,thingtoBuy);

                //for response we call this
                startActivityForResult(intent,REQUEST_CODE_PAYMENT);
            }
        });
    }


     //future payment activity is launch here
        public void onFuturePaymentPressed(View pressed){
            Intent intent = new Intent(MainActivity.this , PayPalFuturePaymentActivity.class);
            startActivityForResult(intent,REQUEST_CODE_FUTURE_PAYMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_PAYMENT){
            if(resultCode == Activity.RESULT_OK){

                //get the payment confirmation here
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirm != null){
                    System.out.println(confirm.toJSONObject().toString());
                    System.out.println(confirm.getPayment().toJSONObject().toString());
                    Toast.makeText(getApplicationContext(), "Order placed",
                            Toast.LENGTH_LONG).show();
                    try {
                        //display the product we buy
                        tv.setText(confirm.getPayment().toJSONObject().getString("short_description"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }else if(requestCode== REQUEST_CODE_FUTURE_PAYMENT){
            if(resultCode == Activity.RESULT_OK){

                //get the paypal authorize code for future payment
                PayPalAuthorization autho = data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                        if(autho != null){
                            Log.i("FuturePaymentExample", autho.toJSONObject()
                                    .toString());
                            String authorize = autho.getAuthorizationCode();

                            Log.i("FuturePaymentExample", authorize);

                            sendAuthorizationToServer(autho);
                            Toast.makeText(getApplicationContext(),
                                    "Future Payment code received from PayPal",
                                    Toast.LENGTH_LONG).show();
                        }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("FuturePaymentExample", "The user canceled.");
        } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
            Log.i("FuturePaymentExample",
                    "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
        }
    }
    private void sendAuthorizationToServer(PayPalAuthorization authorization) {
    }
    public void onFuturePaymentPurchasePressed(View pressed) {
// Get the Application Correlation ID from the SDK
        String correlationId = PayPalConfiguration
                .getApplicationCorrelationId(this);
        Log.i("FuturePaymentExample", "Application Correlation ID: "
                + correlationId);
// TODO: Send correlationId and transaction details to your server for
// processing with
// PayPal...
        Toast.makeText(getApplicationContext(),
                "App Correlation ID received from SDK", Toast.LENGTH_LONG)
                .show();
    }
    @Override
    public void onDestroy() {
// Stop service when done
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
}


