package com.glennsyj.rivals.api.tft.entity;

import com.glennsyj.rivals.api.tft.model.TftLeagueEntryResponse;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

@Embeddable
public class MiniSeries {
    @Column(name = "mini_series_losses")
    private int losses;
    
    @Column(name = "mini_series_target")
    private int target;
    
    @Column(name = "mini_series_wins")
    private int wins;
    
    @Column(name = "mini_series_progress")
    private String progress;

    protected MiniSeries() {}

    public MiniSeries(TftLeagueEntryResponse.MiniSeries response) {
        this.losses = response.losses();
        this.target = response.target();
        this.wins = response.wins();
        this.progress = response.progress();
    }

    public TftLeagueEntryResponse.MiniSeries toResponse() {
        return new TftLeagueEntryResponse.MiniSeries(
                losses, target, wins, progress
        );
    }
} 