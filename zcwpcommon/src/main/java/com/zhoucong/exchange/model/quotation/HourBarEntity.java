package com.zhoucong.exchange.model.quotation;

import com.zhoucong.exchange.model.support.AbstractBarEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Store bars of hour.
 */
@Entity
@Table(name = "hour_bars")
public class HourBarEntity extends AbstractBarEntity {

}
