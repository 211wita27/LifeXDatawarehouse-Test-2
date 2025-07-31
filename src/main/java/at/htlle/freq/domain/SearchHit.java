package at.htlle.freq.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Ergebnisobjekt der globalen Lucene-Suche.
 */
@Data
@AllArgsConstructor
public class SearchHit {
    private String type;   // Entity-Typ, z. B. "Account"
    private int    id;     // Primärschlüssel
    private String text;   // Hauptanzeigetext (Name etc.)
}