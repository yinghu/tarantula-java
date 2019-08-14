var BACCARAT = (function(){
    var _oswaldFamily = 'Oswald, Arial';

    var _tpx_base_v5 = function(cfg){
        const ret={};
        ret.command=function(){};
        const pl = new PIXI.Graphics();
        let b=4;
        let r=16;
        let w=120;
        let h=100;
        if(cfg.h){h=cfg.h;}
        if(cfg.w){w=cfg.w;}
        if(cfg.r){r=cfg.r;}
        if(cfg.b){b=cfg.b;}
        pl.beginFill(0xffffff,1);
        pl.lineStyle(b,0x000000,0.7);
        pl.drawRoundedRect(0,0,w-b,h-b,r);
        pl.endFill();
        if(cfg.t){pl.tint = cfg.t;}else{pl.tint=0x0066ff;}
        if(cfg.a){pl.alpha=cfg.a;}
        pl.interactive=false;
        pl.buttonMode=false;
        pl.cursor='';
        ret.up=function(){pl.tint=0x33ff99;};
        ret.out=function(){pl.tint=0x0066ff;};
        ret.over=function(){pl.tint=0x33ff99;};
        ret.move=function(){};
        pl.on('pointerup',function(){ret.up();});
        pl.on('pointerout',function(){ret.out();});
        pl.on('pointerupoutside',function(){ret.up();});
        pl.on('pointerover',function(){ret.over();});
        pl.on('pointermove',function(){ret.move();});
        pl.on('pointerdown',function(e){ret.command();});
        pl.on('added',function(){});
        ret.base=pl;
        ret.set=function(mode){pl.cursor=mode.cursor;pl.interactive=mode.interactive;pl.buttonMode=mode.interactive;};
        ret.width=w;
        ret.height=h;
        ret.round = r;
        ret.border = b;
        return ret;
     };
     var _tpx_mask_v5 =function(w,h,r,b){
        const p = new PIXI.Graphics();
        p.beginFill(0x000000,1);
        p.lineStyle(b,0x000000,1);
        p.drawRoundedRect(0,0,w-b,h-b,r);
        p.endFill();
        p.width = w;
        p.height = h;
        return p;
     };
     var _tpx_icon_v5 = function(w,h){
          const ic = new PIXI.Sprite(PIXI.Texture.WHITE);
          ic.width = w;
          ic.height = h;
          return ic;
     };
     var _tpx_icon_command = function(w,h){
         const ret={};
         const ctn=new PIXI.Container();
         const frm=_tpx_base_v5({w:w,h:h,r:8,b:4});
         frm.set({cursor:'pointer',interactive:true});
         ctn.addChild(frm.base);
         const ic = _tpx_icon_v5(frm.width,frm.height);
         ic.x = (frm.width-ic.width)/2;
         ic.y = (frm.height-ic.height)/2;
         const msk = _tpx_mask_v5(ic.width*0.9,ic.height*0.9,frm.round*0.8,frm.border);
         msk.x = (frm.width-msk.width)/2;;
         msk.y = (frm.height-msk.height)/2;;
         ic.mask = msk;
         frm.icon = ic;
         ctn.addChild(msk);
         ctn.addChild(ic);
         ret.container = ctn;
         ret.width = frm.width;
         ret.height = frm.height;
         ret.button = frm;
         ret.icon = ic;
         ret.position=function(sc,x,y){ctn.scale.set(sc);ctn.x=x;ctn.y=y;};
         return ret;
    };
    var _tpx_bcc_line = function(pts){
        const p = new PIXI.Graphics();
        p.command = function(){};
        p.beginFill(0xffffff,1);
        p.lineStyle(4,0x000000,0.7);
        p.drawPolygon(pts);
        p.endFill();
        p.tint = Math.random()*0xffffff;
        p.interactive = true;
        p.buttonMode = true;
        p.alpha = 1;
        p.on('pointerover',function(){
            p.alpha = 0.5;
        });
        p.on('pointerout',function(){
            p.alpha = 1;
        });
        p.on('pointerdown',function(e){
            p.alpha = 1;
            p.command(e);
        });

        p.hitArea = new PIXI.Polygon(pts);
        return p;
    };
    var _tpx_bet_line = function(tc){
        let ctn = new PIXI.Container();
        let ret ={};
        let w = 180;
        let h = 150;
        ret.width = w;
        ret.tieBox = new PIXI.Container();
        ret.bankerBox = new PIXI.Container();
        ret.playerBox = new PIXI.Container();
        ret.container = ctn;
        const tline = _tpx_bcc_line([0,50,w/2,100,180,50,180,0,0,0]);
        ret.tieBet = tline;
        ctn.addChild(tline);
        let tt = new PIXI.Text('TIE\n1 : 8',{fontFamily:_oswaldFamily,fontSize: 26,fill: tc,align:'center',leading:5});
        tt.anchor.set(0.5);
        tt.x = w/2;
        tt.y = 50;
        tline.addChild(tt);
        tline.addChild(ret.tieBox);
        const bline = _tpx_bcc_line([0,100,w/2,h,w,100,w,0,w/2,50,0,0]);
        ret.bankerBet = bline;
        bline.y = 60;
        ctn.addChild(bline);
        let tb = new PIXI.Text('BANKER\n1 : 1',{fontFamily: _oswaldFamily,fontSize: 30,fill: tc,align:'center',leading:5});
        tb.anchor.set(0.5);
        tb.x = w/2;
        tb.y = 100;
        bline.addChild(tb);
        bline.addChild(ret.bankerBox);
        const pline = _tpx_bcc_line([0,160,w,160,w,0,w/2,50,0,0]);
        ret.playerBet = pline;
        pline.y = 170;
        ctn.addChild(pline);
        let tp = new PIXI.Text('PLAYER\n1 : 1',{fontFamily: _oswaldFamily,fontSize: 36,fill:tc,align:'center',leading:5});
        tp.anchor.set(0.5);
        tp.x = w/2;
        tp.y = 110;
        pline.addChild(tp);
        pline.addChild(ret.playerBox);
        const pst = _tpx_base_v5({w:w-4,h:76,t:'0x003300'});
        pst.set({cursor:'pointer',interactive:true});
        ret.seat = pst;
        pst.base.x =2;
        pst.base.y = 335;
        pst.base.tint = Math.random()*0xffffff;
        ctn.addChild(pst.base);
        let ic = _tpx_icon_command(60,60);
        ic.container.y = (pst.height-ic.height)/2;
        ic.container.x = 8;
        pst.base.addChild(ic.container);
        let wp = new PIXI.Text('Sit Down',{fontWeight: '500',fontFamily: _oswaldFamily,fontSize: 30,fill: "white",});
        wp.x = 70;
        wp.y = (80-wp.height)/2;
        pst.base.addChild(wp)
        ret.player ={avatar:ic,balance:wp};
        ret.height = 416;
        return ret;
    };
    var _tpx_player_bar = function(){
        let ctn = new PIXI.Container();
        let ret ={};
        let w = 280;
        let h = 80;
        ret.command = function(){};
        ret.container = ctn;
        let pd1 = _tpx_base_v5({w:w,h:h,t:'0x003300'});
        pd1.base.tint = Math.random()*0xffffff;
        ctn.addChild(pd1.base);
        const ps1 = new PIXI.Graphics();
        ps1.beginFill(0xffffff,1);
        ps1.lineStyle(1,0x000000,0.7);
        ps1.drawCircle(0,0,30);
        ps1.endFill();
        ps1.x = w-40;
        ps1.y = 36;
        ctn.addChild(ps1);
        let tp = new PIXI.Text('PLAYER',{fontFamily: _oswaldFamily,fontWeight: '600',fontSize: 30,fill: "yellow",});
        tp.x = pd1.x+h/3;
        tp.y = -15;
        ctn.addChild(tp);
        let wp = new PIXI.Text('9M',{fontWeight: '500',fontFamily: _oswaldFamily,fontSize: 30,fill: "white",});
        wp.x = 15;
        wp.y = (h-wp.height)/2+4;
        ctn.addChild(wp);
        let d1 = new PIXI.Sprite(PIXI.Texture.WHITE);
        d1.width = 60;
        d1.height = 60;
        d1.y = 8;
        d1.x = w-h-70;
        ctn.addChild(d1);
        let tpc = new PIXI.Text('9',{fontFamily: _oswaldFamily,fontWeight: '700',fontSize: 40,fill: "red",});
        tpc.anchor.set(0.5);
        ps1.addChild(tpc);
        ret.width = w;
        ret.height = h;
        let _pv = {view:tpc,rank:0,standing:false,wager:wp};
        ret.setting = _pv;
        ret.set = function(tie){
            if(tie){
                d1.texture = ret.lines[0];
                d1.visible = true;
            }else{
                d1.texture = ret.lines[1];
                d1.visible = true;
            }
        };
        ret.tie = function(){
            d1.texture = ret.lines[2];
            d1.visible = true;
        };
        ret.clear = function(){
            wp.text = '0';
            tpc.text ='-';
            d1.visible = false;
            _pv.standing = false;
        };
        ret.overflow = 15;
        ret.position = function(sc,x,y){ctn.scale.set(sc);ctn.x = x;ctn.y=y};
        return ret;
    };
    var _tpx_banker_bar =function(){
        let ctn = new PIXI.Container();
        let ret ={};
        let w = 280;
        let h = 80;
        ret.command = function(){};
        ret.container = ctn;
        let pd1 = _tpx_base_v5({w:w,h:h,t:'0x003300'});
        pd1.base.tint=Math.random()*0xffffff;
        ctn.addChild(pd1.base);
        const ps1 = new PIXI.Graphics();
        ps1.beginFill(0xffffff,1);
        ps1.lineStyle(2,0x000000,0.7);
        ps1.drawCircle(0,0,30);
        ps1.endFill();
        ps1.x = 40;
        ps1.y = 36;
        ctn.addChild(ps1);
        let tp = new PIXI.Text('BANKER',{fontFamily: _oswaldFamily,fontWeight: '600',fontSize: 30,fill: "yellow",});
        tp.x = w-tp.width-20;
        tp.y = -15;
        ctn.addChild(tp);
        let d1 = new PIXI.Sprite(PIXI.Texture.WHITE);
        d1.width = 60;
        d1.height = 60;
        d1.y = 8;
        d1.x = 80;
        ctn.addChild(d1);
        let wp = new PIXI.Text('9M',{fontWeight: '500',fontFamily: _oswaldFamily,fontSize: 32,fill: "white",});
        wp.x = d1.x+d1.width+10;
        wp.y = (h-wp.height)/2+4;
        ctn.addChild(wp);
        let tpc = new PIXI.Text('9',{fontFamily: _oswaldFamily,fontWeight: '700',fontSize: 40,fill: "red",});
        tpc.anchor.set(0.5);
        ps1.addChild(tpc);
        ret.width = w;
        ret.height = h;
        let _pv = {view:tpc,rank:0,standing:false,wager:wp};
        ret.setting = _pv;
        ret.set = function(tie){
            if(tie){
                d1.texture = ret.lines[0];
                d1.visible = true;
            }else{
                d1.texture = ret.lines[1];
                d1.visible = true;
            }
        };
        ret.tie = function(){
            d1.texture = ret.lines[2];
            d1.visible = true;
        };
        ret.clear = function(){
            wp.text = '0';
            tpc.text ='-';
            d1.visible = false;
            _pv.standing = false;
        };
        ret.overflow = 15;
        ret.position = function(sc,x,y){ctn.scale.set(sc);ctn.x = x;ctn.y=y};
        return ret;
    };
    var _tpx_tie_bar = function(){
        let ctn = new PIXI.Container();
        let ret ={};
        let w = 200;
        let h = 80;
        ret.command = function(){};
        ret.container = ctn;
        const pd1 = _tpx_base_v5({w:w,h:h,t:'0x003300'});
        pd1.base.tint = Math.random()*0xffffff;
        ctn.addChild(pd1.base);
        let tp = new PIXI.Text('TIE',{fontFamily: _oswaldFamily,fontWeight: '600',fontSize: 30,fill: "yellow",});
        tp.x = 20;
        tp.y = -15;
        ctn.addChild(tp);
        let wp = new PIXI.Text('999.99M',{fontWeight: '600',fontFamily: _oswaldFamily,fontSize: 30,fill: "white",});
        wp.x = 15;
        wp.y = (h-wp.height)/2+4;
        ctn.addChild(wp);
        let d1 = new PIXI.Sprite(PIXI.Texture.WHITE);
        d1.width = 60;
        d1.height = 60;
        d1.y = 10;
        d1.x = w-70;
        ctn.addChild(d1);
        ret.wager = wp;
        ret.lines = [];
        ret.set = function(tie){
            if(tie){
                d1.texture = ret.lines[0];
                d1.visible = true;
            }else{
                d1.texture = ret.lines[1];
                d1.visible = true;
            }
        };
        ret.container = ctn;
        ret.clear = function(){
            wp.text = '0';
            d1.visible = false;
        };
        ret.width = w;
        ret.height = h;
        ret.overflow = 15;
        ret.position = function(sc,x,y){ctn.scale.set(sc);ctn.x = x;ctn.y=y};
        return ret;
    };
    var _tpx_dealer_bar = function(){
        let ret ={};
        let _lines = [];
        ret.lines = _lines;
        let _p = _tpx_player_bar();
        _p.lines = _lines;
        let _b = _tpx_banker_bar();
        _b.lines = _lines;
        let _t = _tpx_tie_bar();
        _t.lines = _lines;
        ret.playerBar = _p;
        ret.bankerBar = _b;
        ret.tieBar = _t;
        ret.standing = false;
        let _r = {flag:'',tie:false}
        ret.result =_r;
        ret.set = function(){
            if(_p.setting.standing&&_b.setting.standing){
                let p = _p.setting.rank;
                let b = _b.setting.rank;
                if(p>b){//player won
                    _p.set(true);
                    _b.set(false);
                    _t.set(false);
                    _r.flag = 'P'+p;
                    _r.tie = false;
                }
                else if(p<b){//banker won
                    _p.set(false);
                    _b.set(true);
                    _t.set(false);
                    _r.flag = 'B'+b;
                    _r.tie = false;
                }
                else{//tie won
                     _p.tie();
                     _b.tie();
                     _t.set(true);
                    _r.flag = 'T'+p;
                    _r.tie = true;
                }
                ret.standing = true;
            }
        };
        ret.clear = function(){
            _p.clear();
            _b.clear();
            _t.clear();
            ret.standing = false;
            _r.flag='';
        };
        return ret;
    };
    var _tpx_table = function(n){
        let ctn = new PIXI.Container();
        let ret ={};
        ret.container = ctn;
        ret.width = 0;
        ret.height = 0;
        ret.lines =[];
        let ix;
        let x = 0;
        let cols = n;
        for(ix=0;ix<n;ix++){
            let cl = _tpx_bet_line(ix%2==0?'yellow':'white');
            cl.container.x = x+ix*cl.width;
            cl.container.visible = false;
            ctn.addChild(cl.container);
            ret.width = cl.container.x + cl.width;
            ret.height = cl.height;
            x += 8;
            ret.lines.push(cl);
        }
        ret.position = function(sc,x,y){
             ctn.scale.set(sc);
             ctn.x = x;
             ctn.y = y;
        };
       ret.set = function(md,cc){
           ret.lines[md].container.visible = true;
           ix = 1;
           x = cc-1;
           let flag;
           do{
                flag = false;
               if(x>0){
                flag = true;
               }
               ret.lines[md-ix].container.visible = flag;
               ret.lines[md+ix].container.visible = flag;
               ix++;
               x -=2; //2, 3, 4,
           }while((md+ix)<cols);
        };
        return ret;
    };
    return{
        dealerBar : _tpx_dealer_bar,
        tieBar : _tpx_tie_bar,
        table : _tpx_table,
    };
}());