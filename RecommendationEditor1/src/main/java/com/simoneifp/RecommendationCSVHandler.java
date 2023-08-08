package com.simoneifp;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.commons.text.similarity.FuzzyScore;

public class RecommendationCSVHandler {


    private final String path;
    private List<Recommendation> recommendations;
    private Recommendation header;

    RecommendationCSVHandler(String csvFilePath) {
        this.path = csvFilePath;
    }


    public void save() throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        // The first row in the CSV is always the header, from the skip from load()

        List<Recommendation> recommendations = new ArrayList<>();

        // This combines the header + edited/memory stored recommendations into one object purely for saving.
        recommendations.add(this.header);
        recommendations.addAll(this.recommendations);

        CSVWriter writer = new CSVWriter(new FileWriter(this.path));

        for (Recommendation recommendation : recommendations) {
            String _body = recommendation.getBody();
            if (!Objects.equals(_body, "definition")) {
                if (_body.charAt(0) != 34) {
                    int ch = _body.indexOf(0);
                    _body = "\"" + _body;
                }
                if (_body.charAt(_body.length() - 1) != 34) {
                    int _ch = _body.charAt(_body.length() - 1);
                    _body += "\"";
                }
            }
            String[] rec = new String[5];
            rec[0] = recommendation.getTitle();
            rec[1] = recommendation.getSecondaryTitle();
            rec[2] = recommendation.getStringArchived();
            rec[3] = recommendation.getStringCategory();
            rec[4] = _body;

            writer.writeNext(rec, false);
        }

        writer.close();

    }

    public void load() throws IOException {
        List recommendations = new CsvToBeanBuilder(new FileReader(this.path)).withType(Recommendation.class).withSeparator(',').withIgnoreLeadingWhiteSpace(true).withIgnoreEmptyLine(true).build().parse();
        // Note that this also "reads" the first line. For the sake of display, we need to skip the first "bean" / RecommendationEntry.


        this.header = (Recommendation) recommendations.get(0);

        this.setRecommendations(recommendations.subList(1, recommendations.size()));
        //this.setRecommendations(recommendations);

    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public void addRecommendations(Recommendation recommendation) {
        this.recommendations.add(recommendation);
    }

    public void archiveRecommendation(Recommendation recommendation) {
        recommendation.setArchived(true);
    }

    public void unarchiveRecommendation(Recommendation recommendation) {
        recommendation.setArchived(false);
    }

    public void deleteRecommendation(Recommendation recommendation) {
        this.recommendations.remove(recommendation);
    }

    public List<Recommendation> view() {
        return this.recommendations;
    }

    public List<Recommendation> searchRecommendations(String name, Boolean searchOnlyActive) {
        // Searches recommendations by name from both primary and secondary title, and sorted by highest relevance of either.

        // If the secondary argument is given True, it ignores all Archived recommendations


        FuzzyScore score = new FuzzyScore(Locale.ENGLISH);

        Map<Integer, List<Recommendation>> sorted_dict = new TreeMap<>(Collections.reverseOrder());
        List<Recommendation> recommendations_to_search;

        if (!searchOnlyActive) {
            recommendations_to_search = this.recommendations;
        } else {
            recommendations_to_search = this.recommendations.stream().filter(item -> !item.isArchived()).toList();
        }

        for (Recommendation recommendation_at_i : recommendations_to_search) {
            int secondary_itemscore = score.fuzzyScore(recommendation_at_i.getSecondaryTitle(), name);
            int primary_itemscore = score.fuzzyScore(recommendation_at_i.getTitle(), name);
            int body_itemscore = score.fuzzyScore(recommendation_at_i.getBody(), name);
            int category_itemscore = score.fuzzyScore(recommendation_at_i.getStringCategory(), name);

            // It's a mix of Levenshtein and max relevancy by both titles.

            int title_itemscore = Math.max(primary_itemscore, secondary_itemscore);
            int unionBodyCategory_itemscore = Math.max(body_itemscore, category_itemscore);

            int max_itemscore = Math.max(title_itemscore, unionBodyCategory_itemscore);

            if (sorted_dict.get(max_itemscore) != null) {
                sorted_dict.get(max_itemscore).add(recommendation_at_i);
            } else {
                List<Recommendation> newList = new ArrayList<>();
                newList.add(recommendation_at_i);
                sorted_dict.put(max_itemscore, newList);
            }
        }

        List<Recommendation> processed = new ArrayList<>();

        for (Map.Entry<Integer, List<Recommendation>> entry : sorted_dict.entrySet()) {
            if (entry.getKey() != 0) {
                processed.addAll(entry.getValue());
            }
        }

        // We ignore the 0 entry because they're irrelevant to the search.
        // The zero represents the lower limit; i.e., the search reports only if it matches at least 1+
        // but that's using 3 as our sample size. TODO to be adjusted when every recommendation is in the db.


        // Now it's at its kv, sorted downwards

        return processed;
    }

    public List<Recommendation> viewAllActiveRecommendations(List<Recommendation> recommendations) {
        return recommendations.stream().filter(item -> !item.isArchived()).toList();
    }  //This function doesn't rely on this.recommendations intentionally as it can be filtered both inline or not,
    // depending on how it is utilised

    //return null;
}
