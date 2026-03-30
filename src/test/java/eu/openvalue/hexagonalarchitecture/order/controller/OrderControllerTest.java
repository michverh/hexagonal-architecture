package eu.openvalue.hexagonalarchitecture.order.controller;

import eu.openvalue.hexagonalarchitecture.order.exception.RestExceptionHandler;
import eu.openvalue.hexagonalarchitecture.order.model.FulfillmentType;
import eu.openvalue.hexagonalarchitecture.order.model.Order;
import eu.openvalue.hexagonalarchitecture.order.model.OrderItem;
import eu.openvalue.hexagonalarchitecture.order.model.OrderStatus;
import eu.openvalue.hexagonalarchitecture.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService))
                .setControllerAdvice(new RestExceptionHandler())
                .setMessageConverters(new OrderJsonHttpMessageConverter(this::baseOrder))
                .build();
    }

    @Test
    void createOrderEndpointReturnsPersistedEntity() throws Exception {
        Order response = baseOrder();
        response.setId(41L);

        given(orderService.placeOrder(any(Order.class))).willReturn(response);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderPayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(41));
    }

    @Test
    void updateOrderDelegatesToService() throws Exception {
        Order patch = baseOrder();
        given(orderService.updateOrder(eq(12L), any(Order.class))).willReturn(patch);

        mockMvc.perform(put("/orders/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderPayload()))
                .andExpect(status().isOk());

        verify(orderService).updateOrder(eq(12L), any(Order.class));
    }

    @Test
    void cancelOrderPassesReasonThrough() throws Exception {
        Order order = baseOrder();
        given(orderService.cancelOrder(eq(5L))).willReturn(order);

        mockMvc.perform(post("/orders/5/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Fraud alert\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(orderService).cancelOrder(eq(5L));
        assertThat(reasonCaptor.getValue()).isEqualTo("Fraud alert");
    }

    @Test
    void listOrdersSupportsStatusFilter() throws Exception {
        Order order = baseOrder();
        order.setStatus(OrderStatus.NEW);
        given(orderService.listOrders(Optional.of(OrderStatus.NEW))).willReturn(List.of(order));

        mockMvc.perform(get("/orders").param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerEmail").value("demo@example.com"));
    }

    private Order baseOrder() {
        Order order = new Order();
        order.setCustomerEmail("demo@example.com");
        order.setCustomerName("Demo Customer");
        order.setShippingAddress("Demo Street 1");
        OrderItem item = new OrderItem();
        item.setProductCode("SKU-1");
        item.setProductName("Sample");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setFulfillmentType(FulfillmentType.DIGITAL);
        order.setItems(List.of(item));
        order.setStatus(OrderStatus.NEW);
        return order;
    }

    private String orderPayload() {
        return "{" +
                "\"customerEmail\":\"demo@example.com\"," +
                "\"customerName\":\"Demo Customer\"," +
                "\"shippingAddress\":\"Demo Street 1\"," +
                "\"items\":[{" +
                "\"productCode\":\"SKU-1\"," +
                "\"productName\":\"Sample\"," +
                "\"quantity\":1," +
                "\"unitPrice\":10.00," +
                "\"fulfillmentType\":\"DIGITAL\"}]" +
                "}";
    }

    private static class OrderJsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

        private final Supplier<Order> orderSupplier;

        OrderJsonHttpMessageConverter(Supplier<Order> orderSupplier) {
            super(MediaType.APPLICATION_JSON);
            this.orderSupplier = orderSupplier;
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            return Order.class.isAssignableFrom(clazz)
                    || CancelOrderRequest.class.isAssignableFrom(clazz)
                    || Collection.class.isAssignableFrom(clazz);
        }

        @Override
        protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException {
            if (Order.class.isAssignableFrom(clazz)) {
                return orderSupplier.get();
            }
            if (CancelOrderRequest.class.isAssignableFrom(clazz)) {
                String body = StreamUtils.copyToString(inputMessage.getBody(), StandardCharsets.UTF_8);
                return new CancelOrderRequest(extractValue(body, "reason"));
            }
            throw new HttpMessageNotReadableException("Unsupported payload", inputMessage);
        }

        @Override
        protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException {
            String json;
            if (object instanceof Order order) {
                json = orderToJson(order);
            } else if (object instanceof Collection<?> collection && !collection.isEmpty() && collection.iterator().next() instanceof Order) {
                json = collection.stream()
                        .map(Order.class::cast)
                        .map(this::orderToJson)
                        .collect(Collectors.joining(",", "[", "]"));
            } else {
                json = "{}";
            }
            outputMessage.getBody().write(json.getBytes(StandardCharsets.UTF_8));
        }

        private String orderToJson(Order order) {
            String items = order.getItems().stream()
                    .map(item -> "{" +
                            "\"productCode\":\"" + item.getProductCode() + "\"," +
                            "\"productName\":\"" + item.getProductName() + "\"," +
                            "\"quantity\":" + item.getQuantity() + "," +
                            "\"unitPrice\":" + item.getUnitPrice() + "," +
                            "\"fulfillmentType\":\"" + item.getFulfillmentType() + "\"}")
                    .collect(Collectors.joining(","));
            return "{" +
                    "\"id\":" + nullSafe(order.getId()) + "," +
                    "\"customerEmail\":\"" + order.getCustomerEmail() + "\"," +
                    "\"customerName\":\"" + order.getCustomerName() + "\"," +
                    "\"shippingAddress\":\"" + order.getShippingAddress() + "\"," +
                    "\"status\":\"" + (order.getStatus() == null ? "" : order.getStatus()) + "\"," +
                    "\"items\":[" + items + "]" +
                    "}";
        }

        private String nullSafe(@Nullable Object value) {
            return value == null ? "null" : value.toString();
        }

        private String extractValue(String body, String field) {
            String marker = "\"" + field + "\":";
            int start = body.indexOf(marker);
            if (start < 0) {
                return "";
            }
            int firstQuote = body.indexOf('"', start + marker.length());
            int endQuote = body.indexOf('"', firstQuote + 1);
            if (firstQuote < 0 || endQuote < 0) {
                return "";
            }
            return body.substring(firstQuote + 1, endQuote);
        }
    }
}
