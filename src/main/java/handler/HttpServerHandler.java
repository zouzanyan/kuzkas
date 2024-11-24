package handler;

import com.alibaba.fastjson.JSON;
import entity.ApiResult;
import entity.Cache;
import entity.UriOperationEnum;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import message.SetMessage;
import singleton.CacheSingleton;

import java.nio.charset.StandardCharsets;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final Cache cache = CacheSingleton.getInstance();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        // 处理请求
        handleRequest(ctx, req);
    }

    private void handleRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // 解析请求
        try {
            String uri = req.uri();
            HttpMethod method = req.method();
            String requestBodyString = req.content().toString(StandardCharsets.UTF_8);
            String[] split = uri.split("/");
            String operationSign = split[1];

            if (method.equals(HttpMethod.GET)) {
                if (UriOperationEnum.GET.getUri().equals(operationSign)) {
                    String key = split[2];
                    Object data = OperationHandler.handleGetOperation(key);
                    if (data == null) {
                        sendSuccessResponse(ctx, ApiResult.fail("键值对未找到"));
                        return;
                    }
                    sendSuccessResponse(ctx, ApiResult.success(data));
                } else if (UriOperationEnum.KEYS.getUri().equals(operationSign)) {
                    Object data = OperationHandler.handleAllKeysOperation();
                    sendSuccessResponse(ctx, ApiResult.success(data));
                } else if (UriOperationEnum.ALL.getUri().equals(operationSign)) {
                    Object data = OperationHandler.handleAllKeyValuesOperation();
                    sendSuccessResponse(ctx, ApiResult.success(data));
                } else {
                    sendErrorResponse(ctx, "不支持的请求方法");
                }
            } else if (method.equals(HttpMethod.POST)) {

                if (UriOperationEnum.SET.getUri().equals(operationSign)) {
                    boolean b = OperationHandler.handleSetOperation(requestBodyString);
                    if (!b) {
                        sendSuccessResponse(ctx, ApiResult.fail("设置失败"));
                    }
                    sendSuccessResponse(ctx, ApiResult.success("设置成功"));
                } else if (UriOperationEnum.DEL.getUri().equals(operationSign)) {
                    boolean b = OperationHandler.handleDelOperation(requestBodyString);
                    if (!b) {
                        sendSuccessResponse(ctx, ApiResult.fail("删除失败"));
                    }
                    sendSuccessResponse(ctx, ApiResult.success("删除成功"));
                } else if (UriOperationEnum.EXPIRE.getUri().equals(operationSign)) {
                    boolean b = OperationHandler.handleExpireOperation(requestBodyString);
                    if (!b) {
                        sendSuccessResponse(ctx, ApiResult.fail("失效时间设置失败"));
                    }
                    sendSuccessResponse(ctx, ApiResult.success("失效时间设置成功"));
                }  else if (UriOperationEnum.RPUSH.getUri().equals(operationSign)) {
                    boolean b = OperationHandler.handleRpushOperation(requestBodyString);
                    if (!b) {
                        sendSuccessResponse(ctx, ApiResult.fail("添加失败"));
                    }
                    sendSuccessResponse(ctx, ApiResult.success("添加成功"));
                } else if (UriOperationEnum.LPOP.getUri().equals(operationSign)) {
                    Object data = OperationHandler.handleLpopOperation(requestBodyString);
                    sendSuccessResponse(ctx, ApiResult.success(data));
                } else {
                    sendErrorResponse(ctx, "不支持的请求方法");
                }
            }
        } catch (Exception e) {
            sendErrorResponse(ctx, "请求参数不合法");
            e.printStackTrace();
        }
    }

    private void handleGetOperation(ChannelHandlerContext ctx, FullHttpRequest req) {
        String key = req.uri().substring(5);
        Object value = cache.get(key);
        ApiResult<Object> res;
        if (value != null) {
            res = ApiResult.success(value);
        } else {
            res = ApiResult.fail("键值对未找到");
        }
        sendSuccessResponse(ctx, res);
    }

    private void handleSetOperation(ChannelHandlerContext ctx, FullHttpRequest req) {
        // 解析请求参数
        String content = req.content().toString(StandardCharsets.UTF_8);
        SetMessage setMessage = JSON.parseObject(content, SetMessage.class);
        if (setMessage == null || setMessage.getKey() == null || setMessage.getValue() == null) {
            sendSuccessResponse(ctx, ApiResult.fail("请求参数不合法"));
            return;
        }

        String key = setMessage.getKey();
        Object value = setMessage.getValue();
        Long expireTime = setMessage.getExpireTime();
        cache.set(key, value, expireTime);
        ApiResult<Object> result = ApiResult.success();
        // 将响应消息转换为 JSON 字符串
        sendSuccessResponse(ctx, result);
    }

    // 发送成功请求
    private void sendSuccessResponse(ChannelHandlerContext ctx, ApiResult<Object> res) {
        String s = JSON.toJSONString(res);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(200), Unpooled.wrappedBuffer(s.getBytes(StandardCharsets.UTF_8)));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        // 写入并刷新响应
        ctx.writeAndFlush(response);
    }

    // 发送错误响应
    private void sendErrorResponse(ChannelHandlerContext ctx, String msg) {
        ApiResult<Object> result = ApiResult.fail(msg);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(JSON.toJSONString(result), StandardCharsets.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }
}

