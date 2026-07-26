package com.yxs.springai.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record Companies(@JsonPropertyDescription("公司的名称") String name,
                        @JsonPropertyDescription("公司的地址") String address,
                        @JsonPropertyDescription("公司的投递地址") String resume,
                        @JsonPropertyDescription("公司的描述") String description) {
}
