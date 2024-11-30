package handler;

import com.alibaba.fastjson.JSON;
import entity.ApiResult;
import entity.UriOperationEnum;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

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
            sendErrorResponse(ctx, "Unsupported operation command");
            return;
        }

        if (HttpMethod.GET.equals(method)) {
            handleGetRequest(ctx, operation, tokens);
        } else if (HttpMethod.POST.equals(method)) {
            handlePostRequest(ctx, operation, requestBodyString);
        } else {
            sendErrorResponse(ctx, "Unsupported request method");
        }
    } catch (Exception e) {
        sendErrorResponse(ctx, "The request parameter is illegal");
        e.printStackTrace();
    }
}

    private void handleGetRequest(ChannelHandlerContext ctx, UriOperationEnum operation, String[] tokens) {
        switch (operation) {
            case GET:
                if (tokens.length != 3) {
                    sendErrorResponse(ctx, "the request parameter is invalid");
                    return;
                }
                String key = tokens[2];
                Object data = OperationHandler.handleGetOperation(key);
                if (data == null) {
                    sendSuccessResponse(ctx, ApiResult.fail("Key-value pair not found"));
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
                sendErrorResponse(ctx, "unsupported request methods");
                break;
        }
    }

    private void handlePostRequest(ChannelHandlerContext ctx, UriOperationEnum operation, String requestBodyString) {
        switch (operation) {
            case SET:
                boolean setSuccess = OperationHandler.handleSetOperation(requestBodyString);
                sendSuccessResponse(ctx, setSuccess ? ApiResult.success("Set success") : ApiResult.fail("Set failure"));
                break;
            case DEL:
                boolean delSuccess = OperationHandler.handleDelOperation(requestBodyString);
                sendSuccessResponse(ctx, delSuccess ? ApiResult.success("Del success") : ApiResult.fail("Del failure"));
                break;
            case EXPIRE:
                boolean expireSuccess = OperationHandler.handleExpireOperation(requestBodyString);
                sendSuccessResponse(ctx, expireSuccess ? ApiResult.success("Expire set success") : ApiResult.fail("Expire set failure"));
                break;
            case RPUSH:
                boolean rpushSuccess = OperationHandler.handleRpushOperation(requestBodyString);
                sendSuccessResponse(ctx, rpushSuccess ? ApiResult.success("Rpush success") : ApiResult.fail("Rpush failure"));
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
                sendSuccessResponse(ctx, lpushSuccess ? ApiResult.success("Lpush success") : ApiResult.fail("Lpush failure"));
                break;
            case LLEN:
                Integer llenData = OperationHandler.handleLlenOperation(requestBodyString);
                sendSuccessResponse(ctx, ApiResult.success(llenData));
                break;
            default:
                sendErrorResponse(ctx, "Unsupported operation commands");
                break;
        }
    }


    private String[] checkLegalAndReturnToken(ChannelHandlerContext ctx, String uri) {
        String[] split;
        if (!uri.matches("^/[^/]+(/[^/]+)*$")) {
            sendErrorResponse(ctx, "Illegal URI format");
            return null;
        }
        split = uri.split("/");
        if (split.length < 2) {
            sendErrorResponse(ctx, "Illegal URI format");
            return null;
        }
        return split;
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

