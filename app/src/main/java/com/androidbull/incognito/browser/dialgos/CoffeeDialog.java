package com.androidbull.incognito.browser.dialgos;

import android.app.Dialog;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.ui.MainActivity;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import static com.androidbull.incognito.browser.FacebookLogger.facebookLog;

public class CoffeeDialog extends Dialog implements BillingProcessor.IBillingHandler {
  private Context context;

  public CoffeeDialog(@NonNull Context context) {
    super(context);
    this.context = context;
  }

  private SeekBar seekBar;
  private ImageView mIvCoffee;
  private TextView mTvCoffeePrice;
  private Button mBtnOrderCoffee;
  private int price;
  private BillingProcessor bp;
  private Toast toast;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
    setContentView(R.layout.dialog_coffee);
    facebookLog(context, "Coffee Dialog displayed");
    mTvCoffeePrice = findViewById(R.id.tv_coffee_price);
    mBtnOrderCoffee = findViewById(R.id.btn_order_now);

    bp = new BillingProcessor(context, context.getResources().getString(R.string.base64), this);

    mBtnOrderCoffee.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (price == 2) {
          bp.purchase(MainActivity.mActivity, context.getResources().getString(R.string.coffee_2));
        } else if (price == 5) {
          bp.purchase(MainActivity.mActivity, context.getResources().getString(R.string.coffee_5));
        } else if (price == 10) {
          bp.purchase(MainActivity.mActivity, context.getResources().getString(R.string.coffee_10));
        } else if (price == 20) {
          bp.purchase(MainActivity.mActivity, context.getResources().getString(R.string.coffee_20));
        } else {
          bp.purchase(MainActivity.mActivity, context.getResources().getString(R.string.coffee_2));
        }
        MainActivity.isPaid = true;
      }
    });

    seekBar = findViewById(R.id.sb_coffee);
    mIvCoffee = findViewById(R.id.iv_coffee);
    seekBar.setMax(20);
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      seekBar.setMin(2);
    }
    seekBar.setProgress(2);
    seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
       /* if (progress < 2) {

          progress = 2;
        } else {*/
        if (progress == 3 || progress == 4) {
          progress = 5;
        }
        if (progress == 6 || progress == 7 || progress == 8 || progress == 9) {
          progress = 10;
        }
        if (progress == 11 || progress == 12 || progress == 13 || progress == 14 || progress == 15
            || progress == 16 || progress == 17 || progress == 19 || progress == 18) {
          progress = 20;
        }
//        }
        seekBar.setProgress(progress);
        setCoffeePrice(progress);
        price = progress;
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.getProgress() < 2) {
          Toast.makeText(context, getContext().getString(R.string.coffee_minimum_error_message),
              Toast.LENGTH_SHORT).show();
          seekBar.setProgress(2);
        }
      }
    });


  }

  private void setCoffeePrice(int price) {
    mTvCoffeePrice.setText(price + "$");
  }


  @Override
  public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
    MainActivity.isPaid = true;
    Toast.makeText(context, getContext().getString(R.string.thank_you), Toast.LENGTH_SHORT).show();
    MainActivity.removeHomeBannerAd();
    Log.d("TESTING_COFFEE", "Product purchased");
  }

  @Override
  public void onPurchaseHistoryRestored() {

    Log.d("TESTING_COFFEE", "on purchase history restored");
  }

  @Override
  public void onBillingError(int errorCode, @Nullable Throwable error) {

    Log.d("TESTING_COFFEE",
        "Billing Error: " + error.getLocalizedMessage() + "Error Code: " + errorCode);
  }

  @Override
  public void onBillingInitialized() {
    Log.d("TESTING_COFFEE", "Billing Initialized");

  }
}
