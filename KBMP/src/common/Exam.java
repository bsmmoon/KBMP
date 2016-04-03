package common;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Created by Joey on 3/4/16.
 */
public class Exam {
    private LocalDateTime date;
    private boolean openBook;
    private String venue;
    private Duration duration;

    public static class Builder {
        private LocalDateTime date;
        private boolean openBook;
        private String venue;
        private Duration duration;

        public Exam build() {
            return new Exam(this);
        }

        public Builder setDate(LocalDateTime date) {
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

    private Exam (Builder builder){
        this.date = builder.date;
        this.openBook = builder.openBook;
        this.venue = builder.venue;
        this.duration = builder.duration;
    }

}
