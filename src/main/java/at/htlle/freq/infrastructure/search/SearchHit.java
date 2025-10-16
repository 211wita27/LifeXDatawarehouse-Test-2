
package at.htlle.freq.infrastructure.search;

public class SearchHit {
    private String id;
    private String type;
    private String name;
    private String snippet;

    public SearchHit() {}

    public SearchHit(String id, String type, String name, String snippet) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.snippet = snippet;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    @Override
    public String toString() {
        return "SearchHit{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", snippet='" + snippet + '\'' +
                '}';
    }
}
