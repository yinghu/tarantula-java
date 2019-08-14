var CASINO = (function(){
      var _oswaldFamily = 'Oswald, Arial';
      var _tpx_chip = function(t){
         let ctn = new PIXI.Container();
         let ret ={};
         let w = 100;
         let h = 90;
            ret.width = w;
         ret.height = w;
         ret.container = ctn;
         let p = new PIXI.Graphics();
         let ed = 2*Math.PI/20;
         let st = ed/2;
         let i,x,y,angle;
         for(i=0;i<20;i++){
            if(i%2===0){
                p.beginFill(0xffffff,1);
             }
             else{
                p.beginFill(0xff0000,1);
             }
            p.lineStyle(2,0x000000,1);
            p.arc(0,0,w,st,ed+st);
            p.lineTo(0,0);
            st = st+ed;
         }
         p.beginFill(0x40ff00,1);
         p.drawCircle(0,0,h-30);
         p.endFill();
         let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
         let tt = new PIXI.Text(t,{fontFamily: _oswaldFamily,fontWeight: 700,fontSize:40,fill:'black'});
         tt.anchor.set(0.5);
         //tt.x = (200-tt.width)/2;
         //tt.y = (200-tt.height)/2;
         pl.addChild(tt);
         pl.scale.set(0.4);
         ctn.addChild(pl);
         ret.width = 80;
         ret.height = 80;
         return ret;
     };
      var _tpx_card = function(){
         let ret = {};
         let w = 100;
         let h = 120;
         ret.width = w;
         ret.height = h;
         let tt = new PIXI.Text('K');
         let p = new PIXI.Graphics();
         p.lineStyle(2,0x000000,0.7);
         p.beginFill(0xffffff,1);
         p.drawRoundedRect(0,0,w-2,h-2,12);
         p.endFill();
         let pl =  p;//new PIXI.Sprite(p.generateCanvasTexture());
         let cc = new PIXI.Sprite(PIXI.Texture.WHITE);
         cc.width = w*0.6;
         cc.height = h*0.6;
         cc.x = (w-cc.width-12);
         cc.y = (h-cc.height-12);
         //cc.tint = 0xA09F9B;
         pl.addChild(tt);
         pl.addChild(cc);
         ret.container = pl;
         ret.suit = cc;
         ret.visible =false;
         ret.set = function(c){
             tt.style = {fontWeight: '600',fontFamily: _oswaldFamily,fontSize: 30,fill:c.color};
             tt.text=c.name;
             tt.x=6;
             tt.y=6;
             ret.visible = true;
         };
         return ret;
    };
     var _tpx_hand = function(sz,oz){
        let ret = {};
        ret.width = 0;
        ret.height = 0;
        let hc = new PIXI.Container();
        let pl = new PIXI.Graphics();
        pl.lineStyle(1,0xffffFF,0.5);
        pl.beginFill(0xfff000,1);
        pl.drawCircle(0,0,30);
        pl.endFill();
        let rk = new PIXI.Text('21',{fontFamily: _oswaldFamily,fontSize:30,fill:'black'});
        rk.anchor.set(0.5);
        let clist = [];
        let ix;
        for(ix=0;ix<sz;ix++){
            let crd = _tpx_card();
            ret.height = crd.height;
            crd.container.x = ix*crd.width*oz;
            ret.width = crd.container.x+crd.width;
            crd.container.visible = false;
            hc.addChild(crd.container);
            clist[ix]=crd;
        }
        pl.addChild(rk);
        pl.y = clist[0].height;
        hc.addChild(pl);
        pl.visible = false;
        ret.container = hc;
        ret.cardList = clist;
        ret.rank = rk;
        ret.deck = pl;
        ret.size = sz;
        ret.hand = function(h){
            pl.visible = true;
            rk.text = h.rank;
            clist.forEach(function(c,ix){
                if(c.visible){
                    c.container.x = ix*c.width*oz;
                    ret.width = c.container.x+c.width;
                    c.container.visible = true;
                }
            });
            //hc.pivot.x=(ret.width/2);
            //hc.visible = true;
        };
        ret.fold = function(){
            clist.forEach(function(c,ix){c.container.x = 5*ix;ret.width = c.container.x+c.width;});
        };
        ret.clear = function(){
            pl.visible =false;
            //hc.visible = false;
            clist.forEach(function(c){c.container.visible = false;c.visible=false;});
        };
        ret.position = function(sc,x,y){hc.scale.set(sc);hc.x = x;hc.y=y;};
        return ret;
     };
     var _tpx_turn = function(){
        let ctn = new PIXI.Container();
        let w = 300;
        let h = 120;
        let ret ={};
        ret.width = w+h;
        ret.height = h+10;
        ret.time = '00:00';
        ret.container = ctn;
        let p = new PIXI.Graphics();
        p.beginFill(0x000000,0.3);
        p.lineStyle(1,0x000000,1);
        p.moveTo(h/2,0);
        p.lineTo(h/2+w,0);
        p.arc(h/2+w,h/2,h/2,Math.PI*1.5,Math.PI/2);
        p.lineTo((w+h)/2+10,h);
        p.lineTo((w+h)/2,h+10);
        p.lineTo((w+h)/2-10,h);
        p.lineTo(h/2,h);
        p.arc(h/2,h/2,h/2,Math.PI/2,Math.PI*1.5);
        p.endFill();
        ctn.addChild(p);
        ret.frame = p;
        let _avt = new PIXI.Sprite(PIXI.Texture.WHITE);
        _avt.width = 32;
        _avt.height = 32;
        _avt.x = (w+h)/2-50;
        _avt.y = h-40;
        ctn.addChild(_avt);
        let tn = new PIXI.Text('Waiting for payout',{fontWeight: '600',fontFamily: _oswaldFamily,fontSize:32,fill:'white'});
        tn.anchor.x = 0.5;
        tn.x = (w+h)/2;
        tn.y = tn.height/2+5;
        ctn.addChild(tn);
        ret.message = function(msg){
            tn.text = msg;
            tn.scale.set(1);
            if(tn.width>=(w+h/2)*0.80){
                tn.scale.set((1-(tn.width-(w+h/2))/tn.width)*0.8);
            }
        };
        let tc = new PIXI.Text(ret.time,{fontWeight: '700',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
        ctn.addChild(tc);
        ret.icon = _avt;
        ret.set = function(){
        	tc.text = ret.time;
            tc.x = (w+h)/2;
        	tc.y = h-tc.height-5;
        };
        ret.stop = true;
        ret.prog = 0;
        ret.s1 = 0;
        ret.rid = 0;
        ret.update = function(tm){
            if(stop){
                ret.s1 = tm;
                stop = false;
            }
            ret.prog = tm-ret.s1;
            if(ret.prog>=1000){
                let pt = ret.time.split(':');
                pt[1]--;
                if(pt[1]==0){
                    let m = pt[0]-1;
                    if(m>=0){
                        pt[1]=59;
                        pt[0]=m>0?(m-1):0;
                    }
                }
                pt.forEach(function(t,i){
                    let tv = parseInt(t);
                    if(tv<10&&tv>=0){
                        pt[i]='0'+tv;
                    }
                    else if(tv<0){
                        pt[i]='00';
                    }
                    else{
                        pt[i]=tv;
                    }
                });

                ret.time = pt.join(' : ');
                ret.set();
                ret.s1= tm;
            }
            ret.rid = requestAnimationFrame(ret.update);
        };
        ret.countdown = function(){
            ret.rid = requestAnimationFrame(ret.update);
        };
        ret.cancel = function(){
            ret.stop = true;
            cancelAnimationFrame(ret.rid);
        };
        ret.position = function(sc,x,y){ctn.scale.set(sc);ctn.x = x;ctn.y=y};
        return ret;
    };
    var _tpx_deck = function(){
        let ret = {};
        let w = 160;
        let h = 90;
        ret.width = w;
        ret.height = h;
        let ctn = new PIXI.Container();
        let p = new PIXI.Graphics();
        p.beginFill(0xffffff,1);
        p.lineStyle(4,0x000000,0.7);
        p.drawRoundedRect(0,0,w-4,h-4,5);
        p.endFill();
        let pd = p;//new PIXI.Sprite(p.generateCanvasTexture());
        pd.tint = Math.random()*0xffffff;
        ctn.addChild(pd);
        let bb = new PIXI.Sprite(PIXI.Texture.WHITE);
        bb.tint = Math.random()*0xffffff;
        bb.width = 64
        bb.height = 78;
        bb.x = 5;
        bb.y = h-84;
        ctn.addChild(bb);
        ret.back = bb;
        let tt = new PIXI.Text('214',{fontWeight: '600',fontFamily: _oswaldFamily,fontSize:40,fill:'white'});
        tt.x = (w-bb.width-10)+(w-bb.width-10-tt.width)/5;
        tt.y = (h-tt.height)/2+4;
        ctn.addChild(tt);
        let iot = new PIXI.Text('4-DECK',{fontWeight: '600',fontFamily: _oswaldFamily,fontSize:30,fill:'yellow'});
        iot.x = (w-iot.width)-5;
        iot.y = -15;;
        ctn.addChild(iot);
        ret.deck = function(dk){
            iot.text = dk;
            iot.x = (w-iot.width)-5;
            iot.y = -15;
        };
        ret.container = ctn;
        ret.overflow = 15;
        ret.sequence = function(n){tt.text = n;};
        ret.position = function(sc,x,y){ctn.scale.set(sc),ctn.x = x;ctn.y=y};
        return ret;
     };
     var _tpx_limits = function(){
         let ret = {};
         let w = 160;
         let h = 90;
         ret.width = w;
         ret.height = h;
         let ctn = new PIXI.Container();
         let p = new PIXI.Graphics();
         p.beginFill(0xffffff,1);
         p.lineStyle(4,0x000000,0.7);
         p.drawRoundedRect(0,0,w-4,h-4,5);
         p.endFill();
         let pd = p;//new PIXI.Sprite(p.generateCanvasTexture());
         pd.tint = Math.random()*0xffffff;
         ctn.addChild(pd);
         let tt = new PIXI.Text('Min: 100\nMax: 1000',{fontWeight: '600',fontFamily: _oswaldFamily,fontSize:30,fill:'white'});
         tt.x = 20;
         tt.y = (h-tt.height)/2+4;
         ctn.addChild(tt);
         let iot = new PIXI.Text('LIMITS',{fontWeight: '600',fontFamily: _oswaldFamily,fontSize:30,fill:'yellow'});
         iot.x = 20;
         iot.y = -15;;
         ctn.addChild(iot);
         ret.container = ctn;
         ret.limits = function(m,n){
             tt.text = 'Min: '+m+'\nMax: '+n;
         };
         ret.overflow = 15;
         ret.position = function(sc,x,y){ctn.scale.set(sc),ctn.x = x;ctn.y=y};
         return ret;
     };
    return {
        hand : _tpx_hand,
        chip : _tpx_chip,
        turn : _tpx_turn,
        deck : _tpx_deck,
        limits : _tpx_limits,
    };
})();
