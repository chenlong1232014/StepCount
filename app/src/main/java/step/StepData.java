package step;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

/**
 * Created by Administrator on 2017/6/10.
 */
@Table("step")
public class StepData {

    //自增的主键
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;
    //今天
    @Column("today")
    private String today;
    //步数
    @Column("step")
    private String step;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return "StepData{" +
                "id=" + id +
                ", today='" + today + '\'' +
                ", step='" + step + '\'' +
                '}';
    }
}
