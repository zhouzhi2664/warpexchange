package com.zhoucong.exchange.model.quotation;

import com.zhoucong.exchange.model.support.AbstractBarEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Store bars of day.
 */
@Entity
@Table(name = "day_bars")
public class DayBarEntity extends AbstractBarEntity {

}