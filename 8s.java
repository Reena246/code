package com.company.badgemate.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessGroupDoorId implements Serializable {
    private Long accessGroupId;
    private Long doorId;
}
