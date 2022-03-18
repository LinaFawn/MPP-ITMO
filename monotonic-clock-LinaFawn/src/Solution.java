import org.jetbrains.annotations.NotNull;

/**
 * В теле класса решения разрешено использовать только финальные переменные типа RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author : Ципко Алина
 */
public class Solution implements MonotonicClock {
    private final RegularInt l1 = new RegularInt(0);
    private final RegularInt m1 = new RegularInt(0);
    private final RegularInt r = new RegularInt(0);
    private final RegularInt l2 = new RegularInt(0);
    private final RegularInt m2 = new RegularInt(0);

    @Override
    public void write(@NotNull Time time) {
        l2.setValue(time.getD1());
        m2.setValue(time.getD2());
        r.setValue(time.getD3());
        m1.setValue(time.getD2());
        l1.setValue(time.getD1());
    }

    @NotNull
    @Override
    public Time read() {
        int u1 = l1.getValue();
        int v1 = m1.getValue();
        int w = r.getValue();
        int u2 = l2.getValue();
        int v2 = m2.getValue();

        if (u1 == u2) {
            if (v1 == v2)
                return new Time(u2, v2, w);
            else
                return new Time(u2, v2, 0);
        } else
            return new Time(u2, 0, 0);
    }
}