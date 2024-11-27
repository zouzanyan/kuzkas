package handler;

import com.alibaba.fastjson.JSON;
import entity.ApiResult;
import entity.Cache;
import entity.UriOperationEnum;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import message.PostMessage;
import singleton.CacheSingleton;

import java.nio.charset.StandardCharsets;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    // 懒加载
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
        String operationSign = "";
        String[] tokens = checkLegalAndReturnToken(ctx, uri);
        if (tokens == null) return;
        operationSign = tokens[1];

        UriOperationEnum operation = UriOperationEnum.fromUri(operationSign);
        if (operation == null) {
            sendErrorResponse(ctx, "不支持的请求方法");
            return;
        }

        if (HttpMethod.GET.equals(method)) {
            handleGetRequest(ctx, req, operation, tokens);
        } else if (HttpMethod.POST.equals(method)) {
            handlePostRequest(ctx, req, operation, requestBodyString);
        } else {
            sendErrorResponse(ctx, "不支持的请求方法");
        }
    } catch (Exception e) {
        sendErrorResponse(ctx, "请求参数不合法");
        e.printStackTrace();
    }
}

    private void handleGetRequest(ChannelHandlerContext ctx, FullHttpRequest req, UriOperationEnum operation, String[] tokens) {
        switch (operation) {
            case GET:
                if (tokens.length != 3) {
                    sendErrorResponse(ctx, "请求参数不合法");
                    return;
                }
                String key = tokens[2];
                Object data = OperationHandler.handleGetOperation(key);
                if (data == null) {
                    sendSuccessResponse(ctx, ApiResult.fail("键值对未找到"));
                    return;
                }
                sendSuccessResponse(ctx, ApiResult.success(data));
                break;
            case KEYS:
                Object keysData = OperationHandler.handleAllKeysOperation();
                sendSuccessResponse(ctx, ApiResult.success(keysData));
                break;
            case ALL:
                Object allData = OperationHandler.handleAllKeyValuesOperation();
                sendSuccessResponse(ctx, ApiResult.success(allData));
                break;
            default:
                sendErrorResponse(ctx, "不支持的请求方法");
                break;
        }
    }

    private void handlePostRequest(ChannelHandlerContext ctx, FullHttpRequest req, UriOperationEnum operation, String requestBodyString) {
        switch (operation) {
            case SET:
                boolean setSuccess = OperationHandler.handleSetOperation(requestBodyString);
                sendSuccessResponse(ctx, setSuccess ? ApiResult.success("设置成功") : ApiResult.fail("设置失败"));
                break;
            case DEL:
                boolean delSuccess = OperationHandler.handleDelOperation(requestBodyString);
                sendSuccessResponse(ctx, delSuccess ? ApiResult.success("删除成功") : ApiResult.fail("删除失败"));
                break;
            case EXPIRE:
                boolean expireSuccess = OperationHandler.handleExpireOperation(requestBodyString);
                sendSuccessResponse(ctx, expireSuccess ? ApiResult.success("失效时间设置成功") : ApiResult.fail("失效时间设置失败"));
                break;
            case RPUSH:
                boolean rpushSuccess = OperationHandler.handleRpushOperation(requestBodyString);
                sendSuccessResponse(ctx, rpushSuccess ? ApiResult.success("添加成功") : ApiResult.fail("添加失败"));
                break;
            case LPOP:
                Object lpopData = OperationHandler.handleLpopOperation(requestBodyString);
                sendSuccessResponse(ctx, ApiResult.success(lpopData));
                break;
            case RPOP:
                Object rpopData = OperationHandler.handleRpopOperation(requestBodyString);
                sendSuccessResponse(ctx, ApiResult.success(rpopData));
                break;
            case LPUSH:
                boolean lpushSuccess = OperationHandler.handleLpushOperation(requestBodyString);
                sendSuccessResponse(ctx, lpushSuccess ? ApiResult.success("添加成功") : ApiResult.fail("添加失败"));
                break;
            case LLEN:
                Integer llenData = OperationHandler.handleLlenOperation(requestBodyString);
                sendSuccessResponse(ctx, ApiResult.success(llenData));
                break;
            default:
                sendErrorResponse(ctx, "不支持的请求方法");
                break;
        }
    }


    private String[] checkLegalAndReturnToken(ChannelHandlerContext ctx, String uri) {
        String[] split;
        if (!uri.matches("^/[^/]+(/[^/]+)*$")) {
            sendErrorResponse(ctx, "无效的 URI 格式");
            return null;
        }
        split = uri.split("/");
        if (split.length < 2) {
            sendErrorResponse(ctx, "无效 URI 格式");
            return null;
        }
        return split;
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
        PostMessage setMessage = JSON.parseObject(content, PostMessage.class);
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

