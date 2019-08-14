var SCC = (function(){
     var _oswaldFamily = 'Oswald, Arial';
     var _tpx_scc_header = function(w,h,t){
         let ctn = new PIXI.Container();
         let ret ={};
         ret.command = function(){};
         ret.width = w;
         ret.height = h;
         ret.container = ctn;
         let p = new PIXI.Graphics();
         p.beginFill(0xffffff,0.7);
         p.lineStyle(4,0x000000,1);
         p.drawRoundedRect(0,0,w-4,h-4,8);
         p.endFill();
         let pl =p;// new PIXI.Sprite(p.generateCanvasTexture());
         ret.frame = pl;
         ctn.addChild(pl);
         let t1 = new PIXI.Text(t,{fontWeight: '700',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
         t1.x = (w-t1.width)/2;
         t1.y = -16;//(h-t1.height)/2;
         ctn.addChild(t1);
         ret.overflow = 12;
         let ic = new PIXI.Sprite(PIXI.Texture.WHITE);
         ic.width = h*0.7;
         ic.height =h*0.7;
         ic.visible = false;
         ic.y = (h-ic.height)-8;
         ic.x = (w-ic.width)/2;
         ret.icon = ic;
         ctn.addChild(ic);
         ret.position = function(sc,x,y){ctn.scale.set(sc),ctn.x = x; ctn.y = y;};
         return ret;
     };
      var _tpx_scc_cargo = function(w,h){
         let ctn = new PIXI.Container();
         let ret ={};
         ret.command = function(){};
         ret.width = w;
         ret.height = h;
         ret.container = ctn;
         let p = new PIXI.Graphics();
         p.beginFill(0xffffff,0.7);
         p.lineStyle(4,0x000000,1);
         p.drawRoundedRect(0,0,w-4,h-4,8);
         p.endFill();
         let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
         ret.frame = pl;
         ctn.addChild(pl);
         let t1 = new PIXI.Text('CARGO',{fontWeight: '700',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
         t1.x = (w-t1.width)/2;
         t1.y = -16;//(h-t1.height)/2;
         ctn.addChild(t1);
         ret.overflow = 12;
         let ic = new PIXI.Sprite(PIXI.Texture.WHITE);
         ic.width = h*0.7;
         ic.height =h*0.7;
         ic.visible = false;
         let xg = ((w/2)-ic.width)/2;
         ic.y = (h-ic.height)-8;
         ic.x = (w/2)-ic.width-xg;
         ret.icon1 = ic;
         ctn.addChild(ic);
         let ic1 = new PIXI.Sprite(PIXI.Texture.WHITE);
         ic1.width = h*0.7;
         ic1.height =h*0.7;
         ic1.visible = false;
         ic1.y = (h-ic1.height)-8;
         ic1.x = (w/2)+xg;
         ret.icon2 = ic1;
         ctn.addChild(ic1);
         ret.position = function(sc,x,y){ctn.scale.set(sc),ctn.x = x; ctn.y = y;};
         return ret;
     };
     var _tpx_in_game_header =function(t){
         let ctn = new PIXI.Container();
         let ret ={};
         let w = 320;
         let h = 150;
         ret.command = function(){};
         ret.width = w;
         ret.height = h;
         ret.prizePot = '500M';
         ret.players = 10000;
         ret.rtime = '20:12:03';
         ret.container = ctn;
         let p = new PIXI.Graphics();
         p.beginFill(0xffffff,0.7);
         p.lineStyle(4,0x000000,1);
         p.drawRoundedRect(0,0,w-4,h-4,8);
         p.endFill();
         let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
         pl.alpha = 1;
         ret.frame = pl;
         ctn.addChild(pl);
         let t1 = new PIXI.Text(t,{fontWeight: '700',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
         t1.x = (w-t1.width)/2;
         t1.y = -16;//(h-t1.height)/2;
         ctn.addChild(t1);
         ret.overflow = 12;
         let c = new PIXI.Text('Prize Pot:\nTotal Players:\nRemaining Time:',{fontFamily: _oswaldFamily,fontSize:26,fill:'yellow',leading:4});
         c.x = 16;
         c.y = 30;
         ctn.addChild(c);
         let v = new PIXI.Text(ret.prizePot+'\n'+ret.players+'\n'+ret.rtime,{fontFamily: _oswaldFamily,fontSize:26,fill:'white',leading:4});
         v.y = c.y;
         v.x = c.width+30;
         ctn.addChild(v);
         ret.title = function(n){t1.text=n;};
         ret.position = function(sc,x,y){ctn.scale.set(sc),ctn.x = x; ctn.y = y;};
         ret.set = function(p1,p2,p3){
             ret.prizePot = p1;
             ret.players = p2;
             ret.rtime = p3;
             v.text = ret.prizePot+'\n'+ret.players+'\n'+ret.rtime;
         }
         return ret;
     };
     var _tpx_pot_game_header = function(t){
        let ctn = new PIXI.Container();
         let ret ={};
         let w = 320;
         let h = 200;
         ret.command = function(){};
         ret.width = w;
         ret.height = h;
         ret.prizePot = '500M';
         ret.players = 10000;
         ret.ante = 200;
         ret.container = ctn;
         let p = new PIXI.Graphics();
         p.beginFill(0xffffff,0.7);
         p.lineStyle(4,0x000000,1);
         p.drawRoundedRect(0,0,w-4,h-4,8);
         p.drawRoundedRect(w-120,h-80,100,60,16);
         p.endFill();
         let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
         pl.alpha = 1;
         pl.interactive = true;
         pl.buttonMode = true;
         pl.on('pointerover',function(){
             pl.alpha = 0.5;
         });
         pl.on('pointerout',function(){
             pl.alpha = 1;
         });
         pl.on('pointerdown',function(e){
             ret.command(e);
         });
         ret.frame = pl;
         ctn.addChild(pl);
         let t1 = new PIXI.Text(t,{fontWeight: '700',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
         t1.x = (w-t1.width)/2;
         t1.y = -16;//(h-t1.height)/2;
         ctn.addChild(t1);
         ret.overflow = 12;
         let c = new PIXI.Text('Prize Pot:\nTotal Players:\n\nAnte:',{fontFamily: _oswaldFamily,fontSize:26,fill:'yellow',leading:4});
         c.x = 16;
         c.y = 30;
         ctn.addChild(c);
         let v = new PIXI.Text(ret.prizePot+'\n'+ret.players,{fontFamily: _oswaldFamily,fontSize:30,fill:'white',leading:4});
         v.y = c.y;
         v.x = w/2;
         ctn.addChild(v);
         let a = new PIXI.Text(ret.ante,{fontFamily: _oswaldFamily,fontSize:30,fill:'white',leading:4});
         a.y = h-68;
         a.x = 80;
         ctn.addChild(a);
         let play = new PIXI.Text('PLAY',{fontWeight: '700',fontFamily: _oswaldFamily,fontSize:26,fill:'white',leading:4});
         play.x = w-90;
         play.y = h-65;
         ctn.addChild(play);
         ret.title = function(n){t1.text=n;};
         ret.position = function(sc,x,y){ctn.scale.set(sc),ctn.x = x; ctn.y = y;};
         ret.set = function(p1,p2,p3){
            ret.prizePot = p1;
             ret.players = p2;
             ret.ante = p3;
             v.text = ret.prizePot+'\n'+ret.players;
             a.text = ret.ante;
         }
         return ret;
     };
     var _tpx_scc_slot = function(w,h){
         let ctn = new PIXI.Container();
         let ret ={};
         ret.command = function(){};
         ret.width = w;
         ret.height = h;
         ret.container = ctn;
         let p = new PIXI.Graphics();
         p.beginFill(0xffffff,0.7);
         p.lineStyle(4,0x000000,1);
            p.drawRoundedRect(0,0,w-4,h-4,8);
         p.endFill();
         let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
         pl.alpha = 1;
         pl.interactive = true;
         pl.buttonMode = true;
         pl.on('pointerover',function(){
             pl.alpha = 0.5;
         });
         pl.on('pointerout',function(){
             pl.alpha = 1;
         });
         pl.on('pointerdown',function(e){
             ret.command(e);
         });
         ret.frame = pl;
         ctn.addChild(pl);
         let ic = new PIXI.Sprite(PIXI.Texture.WHITE);
         ic.width = w*0.7;
         ic.height =h*0.7;
         ic.visible = false;
         ic.x = (w-ic.width)/2;
         ic.y = (h-ic.height)/2;
         ret.icon = ic;
         ctn.addChild(ic);
         ret.set = function(active){pl.interactive = active;pl.buttonMode=active;pl.cursor=active?'pointer':'';};
         return ret;
     };
     var _tpx_scc_board = function(r,w,h){
        let ctn = new PIXI.Container();
        let ret ={};
        let ix,icx,x,y;
        let lines = [];
        ret.lines = lines;
        x = 0;
        y = 0;
        for(ix=0;ix<r;ix++){
            for(icx=0;icx<6;icx++){
                let ce = _tpx_scc_slot(w,h);
                ce.container.x = x;
                ce.container.y = y;
                ce.frame.tint = Math.random()*0xffffff;
                ctn.addChild(ce.container);
                x = x+ce.width;
                lines.push(ce);
            }
            x = 0;
            y = y+h;
        }
        ret.width=6*w;
        ret.height=r*h;
        ret.container = ctn;
        ret.position = function(sc,x,y){ctn.scale.set(sc);ctn.x = x;ctn.y = y;};
        ret.set = function(n){
            lines.forEach(function(ce,i){
                ce.frame.tint=Math.random()*0xffffff;
                    ce.container.visible=true;
                    ce.icon.visible = false;
                    if(i>=n){
                        ce.container.visible=false;
                    }
            });
        };
        return ret;
    };
    var _tpx_rank_board = function(n){
        let ctn = new PIXI.Container();
        let ret ={};
        ret.command = function(){ctn.visible=false;};
        ret.width = 600;
        ret.height = 80*(n+1);
        ret.container = ctn;
        let p = new PIXI.Graphics();
        p.beginFill(0x000000,1);
        p.lineStyle(0,0x000000,1);
        p.drawRoundedRect(0,0,ret.width,ret.height,8);
        p.endFill();
        let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
        pl.alpha = 1;
        pl.interactive = true;
        pl.buttonMode = true;
        pl.on('pointerover',function(){
            //pl.alpha = 0.5;
        });
        pl.on('pointerout',function(){
            //pl.alpha = 1;
        });
        pl.on('pointerdown',function(e){
            ret.command(e);
        });
        ctn.addChild(pl);

        let ix;
        let iy = 0;
        let _lines =[];
        for(ix=0;ix<n+1;ix++){
            let r = _tpx_rank_line(600,80);
            r.container.y = iy;
            iy +=r.height;
            if(ix>0){
                r.set('--','--','--','--');
            }
            ctn.addChild(r.container);
            r.frame.tint=Math.random()*0xffffff;
            _lines.push(r);
        }
        ret.lines = _lines;
        ret.position = function(sc,x,y){ctn.scale.set(sc),ctn.x = x; ctn.y = y;};
        ret.open=function(){ctn.visible=true;};
        ret.close=function(){ctn.visible=false;};
        return ret;
    };
    var _tpx_rank_line = function(w,h){
        let ctn = new PIXI.Container();
        let ret ={};
        ret.command = function(){};
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        let p = new PIXI.Graphics();
        p.beginFill(0xffffff,0.7);
        p.lineStyle(4,0x000000,1);
        p.drawRoundedRect(0,0,w-4,h-4,8);
        p.endFill();
        let pl = p;//new PIXI.Sprite(p.generateCanvasTexture());
        ret.frame = pl;
        ctn.addChild(pl);
        let t1 = new PIXI.Text('##',{fontWeight: '500',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
        t1.x = 16
        t1.y = (h-t1.height)/2;
        ctn.addChild(t1);
        let t2 = new PIXI.Text('MMM.MMM',{fontWeight: '500',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
        t2.x = t1.x + t1.width+16;
        t2.y = (h-t2.height)/2;
        ctn.addChild(t2);
        let t3 = new PIXI.Text('MMM.MMM',{fontWeight: '500',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
        t3.x = t2.x+t2.width+16;
        t3.y = (h-t3.height)/2;
        ctn.addChild(t3);
        let t4 = new PIXI.Text('PLAYER',{fontWeight: '500',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
        t4.x = t3.x+t3.width+16;
        t4.y = (h-t4.height)/2;
        console.log(t4.width+"/"+t4.height);
        ctn.addChild(t4);
        let ic = new PIXI.Sprite(PIXI.Texture.WHITE);
        ic.width = h*0.7;
        ic.height =h*0.7;
        ic.visible = true;
        ic.y = (h-ic.height)/2;
        ic.x = (w-ic.width)-8;
        ret.icon = ic;
        ctn.addChild(ic);
        ret.set = function(r,s,p,u){t1.text = r; t2.text=s; t3.text=p;t4.text=u;pl.tint=Math.random()*0xffffff;};
        ret.position = function(sc,x,y){ctn.scale.set(sc),ctn.x = x; ctn.y = y;};
        return ret;
    };
    return{
        table : _tpx_scc_board,
        header : _tpx_scc_header,
        cargo : _tpx_scc_cargo,
        game : _tpx_pot_game_header,
        inGame : _tpx_in_game_header,
        rankBoard : _tpx_rank_board,
    };
})();