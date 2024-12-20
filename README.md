# kuzkas

## 一、项目概述
Kuzkas 基于 Netty 构建高性能kv服务器，采用 JSON 作为通信协议并提供 RESTFul 接口.

## 二、项目介绍



|对比项|                                                           基于 Netty 的缓存项目 |                                                                         Redis                                                                         |
| :-----|-------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------:|
|数据类型支持|                                      SET直接支持多种数据类型存储，无需转换，例如可原生存储复杂业务对象。 | Redis 的 SET 操作主要针对字符串类型。如果要存储更复杂的数据结构，如对象、数组等，就需要先将这些数据结构序列化为字符串。HSET 提供了更灵活的存储方式，尤其是对于存储类似对象的结构，但是它也有一定的复杂性。在操作多个字段时，需要对每个字段进行单独的 HSET 操作或者使用HMSET命令 |
|并发处理| 基于 concurrenthashmap，copyonwritelist等 实现读写并发处理，原生多线程能充分利用多核资源并行处理高并发读写操作 |                                         单线程，执行操作串行化，例如在执行一个非常复杂的 Lua 脚本，且脚本执行时间较长时，所有后续的命令都会排队等待，这会导致响应延迟增加。                                          |
|通信协议与调试|                              采用 JSON 文本协议，代码无需引入特定客户端依赖库，降低依赖复杂度，便于部署维护。 |                                                 拥有高效通信协议，但需特定客户端交互，增加开发环境配置与代码依赖复杂性，且可能面临客户端版本兼容性问题。                                                  |

