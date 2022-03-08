package com.salesmanager.shop.model.catalog.product.attribute;

import java.util.List;

import com.salesmanager.shop.model.catalog.product.attribute.api.ProductOptionValueEntity;

public class PersistableProductOptionValue extends ProductOptionValueEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ProductOptionValueDescription> descriptions;

	public void setDescriptions(List<ProductOptionValueDescription> descriptions) {
		this.descriptions = descriptions;
	}

	public List<ProductOptionValueDescription> getDescriptions() {
		return descriptions;
	}

}