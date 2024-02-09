package com.sdm.presentslk.model;

public class Products {
    private String productId, productName, productDescription, productQty, productPrice, productCategory, imageUrl;

    public Products() {
    }

    public Products(String productId, String productName, String productDescription, String productQty, String productPrice, String productCategory, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productQty = productQty;
        this.productPrice = productPrice;
        this.productCategory = productCategory;
        this.imageUrl = imageUrl;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductQty() {
        return productQty;
    }

    public void setProductQty(String productQty) {
        this.productQty = productQty;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
