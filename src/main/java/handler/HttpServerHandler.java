package handler;

import com.alibaba.fastjson.JSON;
import entity.Cache;
import entity.CacheSingleton;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import res.ApiResult;
import res.SetMessage;

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
        String uri = req.uri();
        HttpMethod method = req.method();

        if (method.equals(HttpMethod.GET)){
            if (uri.startsWith("/get")) {
                handleGetOperation(ctx, req);
            }
        }else if(method.equals(HttpMethod.POST)){
            if (uri.startsWith("/set")){
                handleSetOperation(ctx, req);
            }
        }else{
            sendErrorResponse(ctx, "不支持的请求方法");
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
