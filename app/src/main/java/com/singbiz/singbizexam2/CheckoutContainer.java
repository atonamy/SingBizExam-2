package com.singbiz.singbizexam2;

import android.content.Context;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by archie on 16/2/16.
 */
public class CheckoutContainer {

    private Context currentContext;
    private static SingBizDatabase currentDB = null;
    private static CheckoutContainer currentInstance = null;
    private static Map<Long, ProductItem> checkoutContent = null;

    public CheckoutContainer(Context context) {
        currentContext = context.getApplicationContext();
        if (currentDB == null)
            currentDB = new SingBizDatabase(currentContext);
        if (checkoutContent == null)
            checkoutContent = new HashMap<>();
    }

    public boolean addToCheckout(ProductItem product) {

        if(product != null) {
                if(currentDB.updateProductProperties(product)) {
                    checkoutContent.put(product.productId, product);
                    return true;
                }
            return false;
        }

        return false;
    }

    public boolean removeFromCheckout(ProductItem product) {

        if(product != null) {
            if(currentDB.updateProductProperties(product)) {
                checkoutContent.remove(product.productId);
                return true;
            }
            else
                return false;
        }

        return false;
    }


    public ProductItem getFullProductItem(ProductItem product) {

        if(product != null)
                return checkoutContent.get(product.productId);

        return null;
    }

    public boolean isExist(ProductItem product) {

        boolean result = false;
        if(product != null)
            result = checkoutContent.containsKey(product.productId);
        return result;
    }

    public List<ProductItem> getProducts() {
        List<ProductItem> products = new ArrayList<>();
        List<Long> sortedKeys = new ArrayList(checkoutContent.keySet());
        Collections.sort(sortedKeys);
        for(Long key : sortedKeys)
            products.add(checkoutContent.get(key));
        return products;
    }

    public void sync(int take, int skip) {
        List<ProductItem> products = currentDB.getCheckoutProducts(take, skip);
        sync(products);
    }

    public void sync(List<ProductItem>  products) {
        for(ProductItem product : products)
            checkoutContent.put(product.productId, product);
    }

    public BigDecimal calculateAutomaticDiscount() {
        return null;
    }


    public boolean isEmpty() {
        return (checkoutContent.size() == 0);
    }


    public static BigDecimal discountRule1(List<ProductItem> products) {
        BigDecimal result = BigDecimal.ZERO;
        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(week == Calendar.WEDNESDAY && hour >= 14 && hour <= 18) {
            for(ProductItem product : products)
            {
                double discount = ProductListAdapter.calculateDiscount(product.productPrice, 10.0);
                if(product.getProductFinalDiscount() < discount) {
                    double sub_total = discount * product.getProductQuantity();
                    result = result.add(BigDecimal.valueOf(sub_total));
                    product.setProductFinalDiscount(discount);
                }
            }
        }

        return result;
    }

    public static BigDecimal discountRule2(List<ProductItem> products) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal subTotal = BigDecimal.ZERO;
        List<ProductItem> category_B = new ArrayList<>();
        for(ProductItem product : products)
        {
            if(product.productCategory == ProductItem.PRODUCT_CATEGORY.B) {
                subTotal = subTotal.add(BigDecimal.valueOf(product.productPrice*product.getProductQuantity()));
                category_B.add(product);
            }
        }

        int compare = subTotal.compareTo(BigDecimal.valueOf(1000.0));
        if(compare == 0 || compare == 1) {
            for(ProductItem product : category_B)
            {
                double discount = ProductListAdapter.calculateDiscount(product.productPrice, 40.0);
                if(product.getProductFinalDiscount() < discount) {
                    double sub_total = discount * product.getProductQuantity();
                    result = result.add(BigDecimal.valueOf(sub_total));
                    product.setProductFinalDiscount(discount);
                }
            }
        }

        return result;
    }

    public static BigDecimal discountRule3(List<ProductItem> products) {
        BigDecimal result = BigDecimal.ZERO;
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if(month == Calendar.DECEMBER && day >= 20 && day <= 28) {
            final double discount = 15.0;
            for(ProductItem product : products) {
                if (product.productCategory == ProductItem.PRODUCT_CATEGORY.E && product.getProductFinalDiscount() < 15) {
                    double sub_total = discount * product.getProductQuantity();
                    result = result.add(BigDecimal.valueOf(sub_total));
                    product.setProductFinalDiscount(discount);
                }
            }
        }

        return result;
    }

    public static BigDecimal discountRule4(List<ProductItem> products) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal subTotalA = BigDecimal.ZERO;
        BigDecimal subTotalB = BigDecimal.ZERO;
        List<ProductItem> category_A = new ArrayList<>();
        List<ProductItem> category_B = new ArrayList<>();

        for(ProductItem product : products)
        {
            if(product.productCategory == ProductItem.PRODUCT_CATEGORY.A) {
                subTotalA = subTotalA.add(BigDecimal.valueOf(product.productPrice*product.getProductQuantity()));
                category_A.add(product);
            }
            else if(product.productCategory == ProductItem.PRODUCT_CATEGORY.B) {
                subTotalB = subTotalB.add(BigDecimal.valueOf(product.productPrice*product.getProductQuantity()));
                category_B.add(product);
            }
        }

        if(subTotalA.compareTo(BigDecimal.valueOf(500.0)) == 1 && subTotalB.compareTo(BigDecimal.valueOf(500.0)) == 1) {

            result = result.add(BigDecimal.valueOf(50.0));
            category_A.addAll(category_B);
            BigDecimal totalSize = BigDecimal.ZERO;
            for(ProductItem product : category_A)
                totalSize = totalSize.add(BigDecimal.valueOf(product.getProductQuantity()));
            double discount = BigDecimal.valueOf(50).divide(totalSize, MathContext.DECIMAL64).doubleValue();
            for(ProductItem product : category_A)
                if (product.getProductFinalDiscount() < discount)
                    product.setProductFinalDiscount(discount);
        }

        return result;
    }

    public static void clearDiscounts(List<ProductItem> products) {
        for(ProductItem product : products)
            product.setProductFinalDiscount(0);
    }

}
