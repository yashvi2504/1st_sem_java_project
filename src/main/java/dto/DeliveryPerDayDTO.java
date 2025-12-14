package dto;

import java.util.Date;

public class DeliveryPerDayDTO {

    private Date day;
    private Long total;

    public DeliveryPerDayDTO(Date day, Long total) {
        this.day = day;
        this.total = total;
    }

    public Date getDay() {
        return day;
    }

    public Long getTotal() {
        return total;
    }
}
