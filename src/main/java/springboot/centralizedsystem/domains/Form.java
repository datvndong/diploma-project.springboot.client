package springboot.centralizedsystem.domains;

import java.util.List;

public class Form {

    private String name;
    private String title;
    private String path;
    private long amount;
    private String start;
    private String expired;
    private List<String> tags;
    private int durationPercent;
    private String typeProgressBar;
    private boolean isSubmitted;
    private boolean isPending;
    private boolean isAnonymousForm;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    public int getDurationPercent() {
        return durationPercent;
    }

    public void setDurationPercent(int durationPercent) {
        this.durationPercent = durationPercent;
    }

    public String getTypeProgressBar() {
        return typeProgressBar;
    }

    public void setTypeProgressBar(String typeProgressBar) {
        this.typeProgressBar = typeProgressBar;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean getIsSubmitted() {
        return isSubmitted;
    }

    public void setIsSubmitted(boolean isSubmitted) {
        this.isSubmitted = isSubmitted;
    }

    public boolean getIsPending() {
        return isPending;
    }

    public void setIsPending(boolean isPending) {
        this.isPending = isPending;
    }

    public boolean getIsAnonymousForm() {
        return isAnonymousForm;
    }

    public void setIsAnonymousForm(boolean isAnonymousForm) {
        this.isAnonymousForm = isAnonymousForm;
    }

    public Form(String name, String title, String path, long amount, String start, String expired,
            List<String> tags, int durationPercent, String typeProgressBar, boolean isAnonymousForm) {
        super();
        this.name = name;
        this.title = title;
        this.path = path;
        this.amount = amount;
        this.start = start;
        this.expired = expired;
        this.tags = tags;
        this.durationPercent = durationPercent;
        this.typeProgressBar = typeProgressBar;
        this.isAnonymousForm = isAnonymousForm;
    }

    public Form(String title, String path, String start, String expired, List<String> tags, int durationPercent,
            String typeProgressBar, boolean isSubmitted, boolean isPending) {
        super();
        this.title = title;
        this.path = path;
        this.start = start;
        this.expired = expired;
        this.tags = tags;
        this.durationPercent = durationPercent;
        this.typeProgressBar = typeProgressBar;
        this.isSubmitted = isSubmitted;
        this.isPending = isPending;
    }

    public Form(String title, String path) {
        super();
        this.title = title;
        this.path = path;
    }

    public Form() {
        super();
    }
}
