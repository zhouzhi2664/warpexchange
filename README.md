证券交易系统的交易是基于交易对，例如，BTC/USD交易对表示用USD购买BTC，USD是计价货币（Quote Asset），BTC是交易资产（Base Asset）；
证券交易系统通过买卖双方各自的报价，按照价格优先、时间优先的顺序，对买卖双方进行撮合，实现每秒成千上万的交易量，可以为市场提供高度的流动性和基于微观的价格发现机制。

整个系统从逻辑上可以划分为如下模块：

    API模块（Trading API），交易员下单、撤单的API入口；
    
    定序模块（Sequencer），用于对所有收到的订单进行定序；
    
    交易引擎（Trading Engine），对定序后的订单进行撮合、清算；
    
    行情模块（Quotation），将撮合输出的成交信息汇总，形成K线图；
    
    推送模块（Push），将市场行情、交易结果、资产变化等信息以WebSocket等途径推送给用户；
    
    UI模块（UI），给交易员提供一个Web操作界面，并把交易员的操作转发给后端API。

    以上各模块关系如下：

                                     query
                         ┌───────────────────────────┐
                         │                           │
                         │                           ▼
      ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐
      │ Client  │──▶│   API   │──▶│Sequencer│──▶│ Engine  │
      └─────────┘   └─────────┘   └─────────┘   └─────────┘
                         ▲                           │
                         │                           │
      ┌─────────┐   ┌─────────┐                      │
      │ Browser │──▶│   UI    │                      │
      └─────────┘   └─────────┘                      │
           ▲                                         ▼
           │        ┌─────────┐   ┌─────────┐   ┌─────────┐
           └────────│WebSocket│◀──│  Push   │◀──│Quotation│
                    └─────────┘   └─────────┘   └─────────┘
                    

其中，交易引擎作为最核心的模块，需要一个简单可靠，且模块化程度较高的子系统。

对证券交易系统来说，交易引擎内部可划分为：

    资产模块：管理用户的资产；
    
    订单模块：管理用户的活动订单（即尚未完全成交且未取消的订单）；
    
    撮合引擎：处理买卖订单，生成成交信息；
    
    清算模块：对撮合引擎输出的成交信息进行清算，使买卖双方的资产进行交换。
    
    交易引擎是一个以事件驱动为核心的系统，它的输入是定序后的一个个事件，输出则是撮合结果、市场行情等数据。

    交易引擎内部各模块关系如下：

        ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
           ┌─────────┐    ┌─────────┐
      ──┼─▶│  Order  │───▶│  Match  │ │
           └─────────┘    └─────────┘
        │       │              │      │
                │              │
        │       ▼              ▼      │
           ┌─────────┐    ┌─────────┐
        │  │  Asset  │◀───│Clearing │ │
           └─────────┘    └─────────┘
        └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘

