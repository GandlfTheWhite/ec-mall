import com.zyd.ecmall.service.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderScheduler {

    private final OrderService orderService;

    public OrderScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 5分ごとに実行 / 每5分钟执行一次
     */
    @Scheduled(fixedDelay = 300000) // 300,000ミリ秒 = 5分
    public void cancelTimeoutOrdersJob() {
        orderService.cancelTimeoutOrders();
    }
}
