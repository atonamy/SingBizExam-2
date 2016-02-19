package com.singbiz.singbizexam2;

/**
 * Created by archie on 16/2/16.
 */
public class ProductItem {

    public enum PRODUCT_CATEGORY {
        NONE,
        A,
        B,
        C,
        D,
        E
    }

    public final long productId;
    public final String productReference;
    public final PRODUCT_CATEGORY productCategory;
    public final String productName;
    public final double productPrice;
    public final boolean isCheckout;
    private Integer productQuantity;
    private Double productDiscount;
    private double productFinalDiscount;

    public ProductItem(long id, String reference, PRODUCT_CATEGORY category, String name, double price, boolean checkout) {
        this.productId = id;
        this.productReference = reference;
        this.productCategory = category;
        this.productName = name;
        this.productPrice = price;
        this.productQuantity = null;
        this.productDiscount = null;
        this.isCheckout = checkout;
        this.productFinalDiscount = 0;
    }


    public void setProductQuantity(Integer count) { this.productQuantity = count ;}
    public Integer getProductQuantity() { return productQuantity; }

    public void setProductDiscount(Double value) { this.productDiscount = value ;}
    public Double getProductDiscount() { return productDiscount; }

    public void setProductFinalDiscount(double value) { this.productFinalDiscount = value ;}
    public double getProductFinalDiscount() { return productFinalDiscount; }
}
