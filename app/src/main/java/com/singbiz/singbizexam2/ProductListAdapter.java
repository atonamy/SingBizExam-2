package com.singbiz.singbizexam2;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by archie on 15/2/16.
 */
public class ProductListAdapter extends ArrayAdapter<ProductItem> {

    /*private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    private static int POOL_SIZE = NUMBER_OF_CORES;*/

    private Context mContext;
    private Context appContext;
    private int currentLayout;
    private Cursor currentCursor;
    private View convertView;
    //private ThreadPoolExecutor mDecodeThreadPool;
    //private LinkedBlockingQueue<Runnable> mDecodeWorkQueue;
    private ListView parentListView;
    private static Set<Long> savedCheckbox = null;
    private MenuItem controlButton;
    private ArrayList<ProductItem> allProducts;
    private View currentFooter;


    public ProductListAdapter(ListView parent, ArrayList<ProductItem> products, MenuItem control_button) {
        super(parent.getContext(), 0, products);
        allProducts = products;
        convertView = null;
        /*mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
        mDecodeThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                POOL_SIZE,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mDecodeWorkQueue);*/
        parentListView = parent;
        if(savedCheckbox == null)
            savedCheckbox = new HashSet<>();
        controlButton = control_button;
        currentFooter = null;
    }

    public void setFooter(View footer) {
        currentFooter = footer;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final ProductItem productItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.product_row, parent, false);
        }

        final Handler handler = new Handler();
        final CheckoutContainer checkout = new CheckoutContainer(convertView.getContext());
        final ProductItem checkout_product = new ProductItem(productItem.productId, productItem.productReference,
                productItem.productCategory, productItem.productName, productItem.productPrice, true);

        // Lookup view for data population
        final TextView product_name = (TextView) convertView.findViewById(R.id.productName);
        final TextView product_reference = (TextView) convertView.findViewById(R.id.productReference);
        final TextView product_price = (TextView) convertView.findViewById(R.id.productPrice);
        final TextView product_quantity = (TextView) convertView.findViewById(R.id.textViewQuantity);
        final TextView product_gst = (TextView) convertView.findViewById(R.id.productGst);
        final TextView product_discount = (TextView) convertView.findViewById(R.id.productDiscount);
        final CheckBox selected_product = (CheckBox) convertView.findViewById(R.id.checkboxSelectedProduct);
        selected_product.setFocusable(true);
        product_gst.setText("");
        product_discount.setText("");
        selected_product.setOnCheckedChangeListener(null);
        if(savedCheckbox.contains(productItem.productId))
            selected_product.setChecked(true);
        else
            selected_product.setChecked(false);

        final CompoundButton.OnCheckedChangeListener perform_check = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkout_product.setProductQuantity(1);
                    checkout.addToCheckout(checkout_product);
                    controlButton.setVisible(true);
                } else {
                    checkout_product.setProductQuantity(null);
                    checkout_product.setProductDiscount(null);
                    checkout.removeFromCheckout(checkout_product);
                    if(checkout.isEmpty())
                        controlButton.setVisible(false);
                    savedCheckbox.remove(checkout_product.productId);
                }
            }
        };

       /* final Runnable task = new Runnable() {

            @Override
            public void run() {*/
                if (checkout.isExist(productItem) && !productItem.isCheckout /*&&
                        position >= parentListView.getFirstVisiblePosition() &&
                        position <= parentListView.getLastVisiblePosition() */) {
                    /*handler.post(new Runnable() {
                        @Override
                        public void run() {*/
                    //selected_product.setOnCheckedChangeListener(null);
                    selected_product.setChecked(true);
                    //selected_product.setOnCheckedChangeListener(perform_check);
                    /*if (!selected_product.isEnabled())
                        selected_product.setEnabled(true);*/
                       /* }
                    });*/
                }
                /*else if(!productItem.isCheckout /*&&
                        position >= parentListView.getFirstVisiblePosition() &&
                        position <= parentListView.getLastVisiblePosition() ) {*/
                    /*handler.post(new Runnable() {
                        @Override
                        public void run() {*/
                    /*if (!selected_product.isEnabled())
                        selected_product.setEnabled(true);*/
                       /* }
                    });*/
               // }
        /*    }
        };*/

        // Populate the data into the template view using the data object
        DecimalFormat currency = new DecimalFormat("#,##0.00");
        product_name.setText(productItem.productName);
        product_reference.setText(productItem.productReference);
        product_price.setText("S$"+currency.format(productItem.productPrice));

        if (productItem.isCheckout) {
            selected_product.setFocusable(false);
            product_quantity.setText(productItem.getProductQuantity().toString());
            selected_product.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        savedCheckbox.add(productItem.productId);
                        controlButton.setVisible(true);
                    } else {
                        savedCheckbox.remove(productItem.productId);
                        if (savedCheckbox.size() == 0)
                            controlButton.setVisible(false);
                    }
                }
            });

            double discount = ((productItem.getProductDiscount() == null) ? 0 :
                    productItem.getProductDiscount().doubleValue());
            double final_discount  = calculateDiscount(productItem.productPrice, discount);
            if(productItem.getProductFinalDiscount() < final_discount)
                productItem.setProductFinalDiscount(final_discount);
            product_gst.setText("GST: +S$" + currency.format(calculateGST(productItem.productPrice - productItem.getProductFinalDiscount())));
            product_discount.setText("Discount: -S$" + currency.format(productItem.getProductFinalDiscount()));
        }
        else
            selected_product.setOnCheckedChangeListener(perform_check);



            //mDecodeThreadPool.execute(task);

            // Return the completed view to render on screen

        if(position == allProducts.size()-1)
            updateFooter();
        return convertView;
    }

    public int productsCount() {
       return allProducts.size();
    }

    public void deleteSelected() {

        CheckoutContainer checkout = new CheckoutContainer(parentListView.getContext());
        int shift_count = 0;
        ArrayList<ProductItem> deletedItems = new ArrayList<ProductItem>();
        for(Long delete : savedCheckbox) {
            ProductItem product = null;
            for(ProductItem p : allProducts)
                if(p.productId == delete)
                    product = p;
            if(product != null) {
                product.setProductQuantity(null);
                product.setProductDiscount(null);
                deletedItems.add(product);
            }
        }
        for(ProductItem delete : deletedItems) {
            checkout.removeFromCheckout(delete);
            remove(delete);
        }
        deletedItems.clear();
        savedCheckbox.clear();
        CheckoutContainer.clearDiscounts(allProducts);
        applyAutomaticDiscount();
        notifyDataSetChanged();
        controlButton.setVisible(false);
        if(allProducts.size() == 0 && currentFooter != null)
            parentListView.removeFooterView(currentFooter);
    }

    public void updateProduct(ProductItem product, int position) {
        CheckoutContainer checkout = new CheckoutContainer(parentListView.getContext());
        allProducts.get(position).setProductDiscount(product.getProductDiscount());
        allProducts.get(position).setProductQuantity(product.getProductQuantity());
        allProducts.get(position).setProductFinalDiscount(0);
        checkout.addToCheckout(product);
        applyAutomaticDiscount();
        notifyDataSetChanged();
    }

    public void updateFooter() {

        if(allProducts.size() > 0 && currentFooter != null) {
            DecimalFormat currency = new DecimalFormat("#,##0.00");
            TextView total_quantity = (TextView)currentFooter.findViewById(R.id.textViewTotalQuantity);
            TextView total_gst = (TextView)currentFooter.findViewById(R.id.textViewTotalGST);
            TextView total_discount = (TextView)currentFooter.findViewById(R.id.textViewTotalDiscount);
            TextView total_amount = (TextView)currentFooter.findViewById(R.id.textViewTotalAmount);

            BigInteger totalQuantity = BigInteger.ZERO;
            BigDecimal totalGst = BigDecimal.ZERO;
            BigDecimal totalDiscount = BigDecimal.ZERO;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for(ProductItem product : allProducts) {
                double gst = calculateGST(product.productPrice - product.getProductFinalDiscount());
                double current_discount = product.getProductFinalDiscount()*product.getProductQuantity();
                double subtotal = product.productPrice * product.getProductQuantity();
                double subtotal_gst = gst*product.getProductQuantity();
                subtotal -= current_discount;
                totalQuantity = totalQuantity.add(BigInteger.valueOf(product.getProductQuantity()));
                totalGst = totalGst.add(BigDecimal.valueOf(subtotal_gst));
                totalDiscount = totalDiscount.add(BigDecimal.valueOf(current_discount));
                totalAmount = totalAmount.add(BigDecimal.valueOf(subtotal + subtotal_gst));
            }

            total_quantity.setText("Total quantity: "  + totalQuantity.toString());
            total_gst.setText("Total GST: +S$" + currency.format(totalGst));
            total_discount.setText("Total discount: -S$" + currency.format(totalDiscount));
            total_amount.setText("Total amount: S$" + currency.format(totalAmount));
        }

    }

    protected static double calculateGST(double price) {
        return calculateDiscount(price, 7);
    }

    public static double calculateDiscount(double price, double discount) {
        double gst = price / 100.0 * discount;
        return (double)Math.round(gst * 100.0)/100.0;
    }

    public void applyAutomaticDiscount() {
        CheckoutContainer.discountRule1(allProducts);
        CheckoutContainer.discountRule2(allProducts);
        CheckoutContainer.discountRule3(allProducts);
        CheckoutContainer.discountRule4(allProducts);
    }

    public boolean isNoSelection() {
        return (savedCheckbox.size() == 0);
    }
}
