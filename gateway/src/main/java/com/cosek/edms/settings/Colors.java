package com.cosek.edms.settings;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Colors {
    private String primaryColor;
    private String primaryForeground;
    private String ring;
    private String card;
    private String background;
    private String foreground;
    private String secondaryColor;
    private String accent;
}
