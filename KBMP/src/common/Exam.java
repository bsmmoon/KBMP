package common;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Created by Joey on 3/4/16.
 */
public class Exam implements Serializable {
    private OffsetDateTime date = null;
    private boolean openBook = false;
    private String venue = "";
    private Duration duration = Duration.ZERO;

    public static class Builder {
        private OffsetDateTime date;
        private boolean openBook;
        private String venue;
        private Duration duration;

        public Exam build() {
            return new Exam(this);
        }

        public Builder setDate(OffsetDateTime date) {
            this.date = date;
            return this;
        }

        public Builder setOpenBook(boolean openBook){
            this.openBook = openBook;
            return this;
        }
        public Builder setVenue(String venue){
            this.venue = venue;
            return this;
        }

        public Builder setDuration(Duration duration){
            this.duration = duration;
            return this;
        }
    }

    public static Builder builder(){
        return new Builder();
    }

    public Exam() {}

    private Exam (Builder builder){
        this.date = builder.date;
        this.openBook = builder.openBook;
        this.venue = builder.venue;
        this.duration = builder.duration;
    }

}
