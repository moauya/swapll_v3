package com.swapll.gradu.dto;

import com.swapll.gradu.Enum.PaymentMethod;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

@Setter
@Getter
public class SearchDTO {
    private String keyword;
    private Integer categoryId;
    private Integer minPrice;
    private Integer maxPrice;
    private PaymentMethod paymentMethod;
}
