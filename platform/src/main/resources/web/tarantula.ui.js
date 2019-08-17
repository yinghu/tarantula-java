var UI = (function(){
     //var _robotoFamily = "Roboto,Arial";//Arial as backup
     //var _robotoDensFamily = "\'Roboto Condensed\',Arial";//Arial as backup
     var _oswaldFamily = 'Oswald, Arial';
     var _felipaFamily = 'Felipa, Arial';
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
        pl.lineStyle(b,0x000000,0.7);
        pl.beginFill(0xffffff,1);
        pl.drawRoundedRect(0,0,w-b,h-b,r);
        pl.endFill();
        if(cfg.t){pl.tint = cfg.t;}else{pl.tint=0x0066ff;}
        if(cfg.a){pl.alpha=cfg.a;}
        pl.interactive=false;
        pl.buttonMode=false;
        pl.cursor='';
        ret.up=function(){pl.tint=0x0066ff;};
        ret.out=function(){pl.tint=0x0066ff;};
        ret.over=function(){pl.tint=0x339933;};
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
     var _tpx_icon_v5 = function(w,h){
         const ic = new PIXI.Sprite(PIXI.Texture.WHITE);
         ic.width = w;
         ic.height = h;
         return ic;
     };
     var _tpx_text_v5 = function(fw,fz,fm,fi){
        return new PIXI.Text('',{fontWeight:fw,fontFamily:fm,fontSize:fz,fill:fi});
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
     var _tpx_cashin_view = function(){
        const ctn = new PIXI.Container();
        const w = 560;
        const h = 400;
        const ret ={};
        ret.close = function(){};
        ret.width = w;
        ret.height = h;
        ret.balance = 6900009998;
        ret.name = 'Andured Wise';
        ret.container = ctn;
        const bk = _tpx_base_v5({w:w,h:h,r:20,b:8,t:'0x003300'});
        ctn.addChild(bk.base);
        const ic = _tpx_icon_command(84,84);
        ic.container.x = w-ic.width-16;
        ic.container.y = 16;
        ctn.addChild(ic.container);
        ret.close = ic;

        const ip = _tpx_icon_command(120,120);
        ip.container.x = 30;
        ip.container.y = 30;
        ctn.addChild(ip.container);
        ret.avatar = ip;

        const n = new PIXI.Text(ret.name,{fontFamily:_oswaldFamily,fontSize:40,fill:'yellow'});
        ctn.addChild(n);
        const t = new PIXI.Text(ret.balance,{fontFamily:_oswaldFamily,fontSize:40,fill:'white'});
        ctn.addChild(t);
        ret.set = function(){
            n.text = ret.name;
            n.scale.set(1);
            if(n.width>=(w-150)*0.85){
                n.scale.set((1-(n.width-(w-150))/n.width)*0.85);
            }
            n.x = 150;
            n.y = 30;
            t.text = ret.balance;
            t.scale.set(1);
            if(t.width>=(w-150)*0.85){
                t.scale.set((1-(t.width-(w-150))/t.width)*0.85);
            }
            t.x = 150;
            t.y = n.y+n.height+12;

        };
        const cashin = _tpx_text_command(160,120,'CashIn');
        cashin.container.y = h-cashin.height-20;
        cashin.container.x = w-cashin.width-30;
        ctn.addChild(cashin.container);
        ret.cashin = cashin;
        const buy = _tpx_text_command(160,120,'Buy');
        buy.container.y = h-cashin.height-20;
        buy.container.x = 30;
        ctn.addChild(buy.container);
        ret.buy = buy;
        ret.position = function(sc,x,y){
            ctn.scale.set(sc);
            ctn.x = x;
            ctn.y = y;
        };
        ctn.visible = false;
        ret.open = function(){ctn.visible = true;};
        return ret;
    };
    var _tpx_balance_view = function(){
        let ctn = new PIXI.Container();
        let w = 280;
        let h = 96;
        let ret ={};
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        const frm = _tpx_base_v5({w:w,h:h,r:8,t:'0x00b300'});
        frm.set({cursor:'pointer',interactive:true});
        ctn.addChild(frm.base);
        ret.button=frm;
        const ip = _tpx_icon_command(86,86);
        ip.container.x = 5;
        ip.container.y = 5;
        ctn.addChild(ip.container);
        ret.avatar = ip;
        let n = new PIXI.Text('name',{fontFamily:_oswaldFamily,fontSize:28,fill:'yellow'});
        n.x = 96;
        n.y = 12;
        ctn.addChild(n);
        let t = new PIXI.Text('0000000',{fontFamily:_oswaldFamily,fontSize:32,fill:'white'});
        t.x = 96;
        t.y = h-t.height;
        ctn.addChild(t);
        ret.name = function(tn){
            n.text = tn;
            n.scale.set(1);
            if(n.width>=(w-96)*0.80){
                n.scale.set((1-(n.width-(w-96))/n.width)*0.8);
            }
            n.x = 96;
            n.y = h/2-n.height;
        };
        ret.balance = function(tn){
            t.text = tn;
            t.scale.set(1);
            if(t.width>=(w-96)*0.80){
                t.scale.set((1-(t.width-(w-96))/t.width)*0.8);
            }
            t.x = 96;
            t.y = h/2+(h/2-t.height)/2;
        };
        ret.amount = 0;
        ret.amountBase =0;
        ret.start = 0;
        ret.duration = 1000;
        ret.update = function(nb){
            requestAnimationFrame(function(tm){
                ret.start = tm;
                ret.amountBase = ret.amount;
                ret.amount = nb;
                requestAnimationFrame(ret.frame);
            });
        };
        ret.frame = function(tm){
            let ds = tm - ret.start;
            if(ds<ret.duration){
                let delta = ((ret.amount-ret.amountBase)*(ds/ret.duration)).toFixed(2);
                if(delta>0){
                    delta = (parseFloat(delta) + parseFloat(ret.amountBase)).toFixed(2);
                }
                else{
                    delta = (ret.amountBase-Math.abs(delta)).toFixed(2);
                }
                ret.balance(delta);
                requestAnimationFrame(ret.frame);
            }
            else{
                ret.balance(ret.amount);
            }
        };
        ret.position = function(sc,x,y){ctn.scale.set(sc);ctn.x = x;ctn.y = y;};
        return ret;
    };
     var  _tpx_application_shortcut = function(){
        let ctn = new PIXI.Container();
        let w = 280;
        let h = 140;
        let ret ={};
        ret.width = w;
        ret.height = h;
        const frm = _tpx_base_v5({w:w,h:h,t:'0x6600ff'});
        frm.set({cursor:'pointer',interactive:true});
        ctn.addChild(frm.base);
        ret.button=frm;
        let t = new PIXI.Text('NAME',{fontWeight: '700',fontFamily: _felipaFamily,fontSize:h/2,fill:'white'});
        t.anchor.set(0.5);
        t.x = (w)/2;
        t.y = h/2;
        ctn.addChild(t);
        ret.set = function(tn){
            t.text = tn;
            t.scale.set(1);
            if(t.width>=w*0.80){
                t.scale.set((1-(t.width-w)/t.width)*0.8);
            }
        }
        ret.container = ctn;
        return ret;
    };
    var _tpx_application_header = function(){
         const ctn = new PIXI.Container();
         const w = 450;
         const h = 350;
         const ret ={};
         ret.width = w;
         ret.height = h;
         ret.container = ctn;
         ctn.addChild(_tpx_base_v5({w:w,h:h,t:'0x6600ff'}).base);
         const frules =  _tpx_text_command(160,120,'Rules');
         frules.container.scale.set(0.5);
         frules.container.x = 16;
         frules.container.y = 16;
         ctn.addChild(frules.container);
         ret.rules = frules;
         const t = new PIXI.Text('VIP',{fontWeight: '700',fontFamily: _felipaFamily,fontSize:40,fill:'white'});
         t.anchor.x =(0.5);
         t.x = (w)/2+40;
         t.y = 16;
         ctn.addChild(t);
         const c = new PIXI.Text('\nPlayMode:\nDeckSize:\nWagerLimits:\nDealerFee:\nMaxPlayers:\nCashIn:',{fontFamily: _oswaldFamily,fontSize:32,fill:'yellow',leading:5});
         c.x = 16;
         c.y = 55;
         ctn.addChild(c);
         const v = new PIXI.Text('\n600K\nNo Limited\n5M',{fontFamily: _oswaldFamily,fontSize:32,fill:'white',leading:5});
         v.x = 170;
         v.y = 55;
         ctn.addChild(v);
         ret.set = function(n,w1,w2,w3,w4,w5,w6){
            t.text = n;
            v.text = '\n'+w1+'\n'+w2+'\n'+w3+'\n'+w4+'\n'+w5+'\n'+w6;
         };
         const fplay = _tpx_text_command(200,168,'Play');
         fplay.container.scale.set(0.5);
         fplay.container.x = w-fplay.width*0.5-16;
         fplay.container.y = h-fplay.height*0.5-16;
         ctn.addChild(fplay.container);
         ret.play = fplay;
         return ret;
    };
    var _tpx_instance_header = function(){
       const ctn = new PIXI.Container();
       const w = 450;
       const h = 100;
       const ret ={};
       ret.width = w;
       ret.height = h;
       ret.container = ctn;
       ctn.addChild(_tpx_base_v5({w:w,h:h,r:8,b:2,t:'0x9900ff'}).base);
       const t = new PIXI.Text('Players (2/10)',{fontFamily:_oswaldFamily,fontSize:32,fill:'yellow'});
       t.x = 16;
       t.y =  (h-t.height)/2;
       ctn.addChild(t);
       ret.status = t;
       const fplay = _tpx_text_command(200,168,'Join');
       fplay.container.scale.set(0.5);
       fplay.container.x = w-fplay.width*0.5-10;
       fplay.container.y = (h-fplay.height*0.5)/2;
       ctn.addChild(fplay.container);
       ret.join = fplay;
       return ret;
    };
    var _tpx_application_list = function(n){
        let ctn = new PIXI.Container();
        let ret ={};
        ret.container = ctn;
        let h1 = _tpx_application_header();
        ctn.addChild(h1.container);
        ret.application = h1;
        let ins = _tpx_instance_list(n);
        ins.container.x = 0;
        ins.container.y = h1.height+5;
        ret.width = h1.width;
        ret.height = h1.height+ins.height;
        ret.instances = ins;
        ret.index = 0;
        ctn.addChild(ins.container);
        ctn.visible = false;
        return ret;
    };
    var _tpx_instance_list = function(n){
       let ctn = new PIXI.Container();
       let w =0;
       let h =0;
       let ret ={};
       ret.container = ctn;
       let i;
       let hlist=[]
       for(i=0;i<n;i++){
        let h1 = _tpx_instance_header();
        h1.container.y = i*(h1.height+5);
        ctn.addChild(h1.container);
        hlist.push(h1);
           w = h1.width;
           h = h+(h1.height+5);
       }
       ret.width = w;
       ret.height = h;
       ret.list = hlist;
       return ret;
    };
    var _tpx_confirmation =function(){
        const ctn = new PIXI.Container();
        const w = 560;
        const h = 400;
        const ret ={};
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        ctn.visible = false;
        ctn.addChild(_tpx_base_v5({w:w,h:h,r:20,b:8,t:'0x003300'}).base);
        const t = new PIXI.Text('message',{fontFamily:_oswaldFamily,fontSize:32,fill:'yellow'});
        ctn.addChild(t);
        ret.message = function(msg){
            t.text = msg;
            t.scale.set(1);
            if(t.width>=(w)*0.95){
                t.scale.set((1-(t.width-(w))/t.width)*0.95);
            }
            t.x = (w-t.width)/2;
            t.y = (h-t.height-80)/2;
        }
        let fyes = _tpx_text_command(120,100,'YES');
        fyes.container.x = w-fyes.width-30;
        fyes.container.y = h-fyes.height-20;
        ctn.addChild(fyes.container);
        ret.yes = fyes;
        let fno = _tpx_text_command(120,100,'NO');
        fno.container.x = 30;
        fno.container.y = h-fno.height-20;
        ret.no = fno;
        ctn.addChild(fno.container);
        ret.position = function(sc,x,y){
            ctn.scale.set(sc);
            ctn.x = x;
            ctn.y = y;
        };
        ret.close = function(){ctn.visible = false;};
        ret.open = function(){ctn.visible = true;};
        return ret;
    };
    var _tpx_exit = function(){
       const ctn = new PIXI.Container();
       const w = 560;
       const h = 400;
       const ret ={};
       ret.width = w;
       ret.height = h;
       ret.container = ctn;
       const bk = _tpx_base_v5({w:w,h:h,r:20,b:8,t:'0x003300'});
       ctn.addChild(bk.base);
       const ic = _tpx_icon_command(84,84);
       ic.container.x = w-ic.width-16;
       ic.container.y = 16;
       ctn.addChild(ic.container);
       ret.close = ic;
       const logout = _tpx_text_command(250,100,'Log Out');
       logout.container.x = (w-logout.width)/2;
       logout.container.y = h/2 - 100;
       ctn.addChild(logout.container);
       ret.logout = logout;
       const toHome = _tpx_text_command(250,100,'To Home Page');
       toHome.container.x = (w-toHome.width)/2;
       toHome.container.y = h/2 + 20;
       ctn.addChild(toHome.container);
       ret.home = toHome;
       ret.position = function(sc,x,y){ctn.scale.set(sc);ctn.x = x;ctn.y = y;};
       ret.hide = function(){ctn.visible = false;};
       ret.open = function(){ctn.visible = true;};
       return ret;
    };
    var _tpx_text_command = function(w,h,title){
        const ret={};
        const ctn=new PIXI.Container();
        const frm=_tpx_base_v5({w:w,h:h,r:16,b:4});
        frm.set({cursor:'pointer',interactive:true});
        ctn.addChild(frm.base);
        const tx = _tpx_text_v5(600,frm.height/2,_oswaldFamily,'white');
        ret.title=function(t){
            tx.text=t;
            tx.scale.set(1);
            if(tx.width>=frm.width*0.8){tx.scale.set((1-(tx.width-frm.width)/tx.width)*0.8);}
            tx.x=(frm.width-tx.width)/2;
            tx.y=(frm.height-tx.height)/2;
        };
        ret.title(title);
        ctn.addChild(tx);
        ret.container = ctn;
        ret.width = frm.width;
        ret.height = frm.height;
        ret.button = frm;
        ret.position=function(sc,x,y){ctn.scale.set(sc);ctn.x=x;ctn.y=y;};
        return ret;
    };

    var _tpx_command_list = function(n,w){
        let ctn = new PIXI.Container();
        let ret ={};
        ret.container = ctn;
        ret.width = 0;
        ret.height = 0;
        ret.n = n;
        ret.list =[];
        let ix;
        let x = 0;
        for(ix=0;ix<n;ix++){
            let cl = _tpx_text_command(w,96,'Button');
            cl.container.x = x+ix*cl.width;
            ctn.addChild(cl.container);
            ret.width = cl.container.x + cl.width;
            ret.height = cl.height;
            x += 5;
            ret.list.push(cl);
        }
        ret.position = function(sc,x,y){
             ctn.scale.set(sc);
             ctn.x = x;
             ctn.y = y;
        };
        return ret;
    };
    var _tpx_avatar=function(w,h){
        const ctn = new PIXI.Container();
        const ret={};
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        const body =_tpx_icon_v5(w,h);
        const kit =_tpx_icon_v5(w,h);
        const face =_tpx_icon_v5(w,h);
        const hair =_tpx_icon_v5(w,h);
        const msk = _tpx_mask_v5(w,h*0.7,8,2);
        ctn.mask = msk;
        ctn.addChild(msk);
        ctn.addChild(body);
        ctn.addChild(kit);
        ctn.addChild(face);
        ctn.addChild(hair);
        ret.body = body;
        ret.kit = kit;
        ret.face = face;
        ret.hair = hair;
        ret.position=function(sc,x,y){ctn.scale.set(sc);ctn.x=x;ctn.y=y;};
        return ret;
    }
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

    var _tpx_menu = function(rn){
        let ctn = new PIXI.Container();
        let ret ={};
        let w = 96;
        let h = 96;
        ret.width = w*4;
        ret.height = h*rn;
        ret.container = ctn;
        ctn.visible = false;
        let r,c;
        let ic;
        let x = 0;
        let y = 0;
        let list=[];
        for(r=0;r<rn;r++){
            for(c=0;c<4;c++){
                ic = _tpx_icon_command(w,h);
                ic.container.x = x;
                ic.container.y = y;
                ctn.addChild(ic.container);
                list.push(ic);
                x = x+w;
            }
            x=0;
            y=y+h;
        }
        ret.list = list;
        ret.position = function(sc,x,y){ctn.scale.set(sc);ctn.x = x;ctn.y = y;};
        ret.open = function(){ctn.visible =true;};
        ret.close = function(){ctn.visible =false;};
        return ret;
    };
    var _tpx_wager_bar = function(){
        const ctn = new PIXI.Container();
        const w = 280;
        const h = 96;
        const ret ={};
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        ret.wager = 1000;
        ctn.addChild(_tpx_base_v5({w:w,h:h,r:8,t:'0x00b300'}).base);
        const wg = new PIXI.Text(ret.wager,{fontFamily:_oswaldFamily,fontSize:32,fill:'yellow'});
        wg.anchor.set(0.5);
        wg.x = (ret.width/2);
        wg.y = (ret.height/2);
        ctn.addChild(wg);
        const _pr = _tpx_icon_command(64,64);
        _pr.container.x = w-_pr.width-8;
        _pr.container.y = (h-_pr.height)/2;
        ctn.addChild(_pr.container);
        const _pf = _tpx_icon_command(64,64);
        _pf.container.x = 8;
        _pf.container.y = (h-_pf.height)/2;
        ctn.addChild(_pf.container);
        ret.plus = _pr;
        ret.minus = _pf;
        ret.min = 100;
        ret.max = 1000;
        ret.wager = 100;
        ret.set = function(){
            wg.text = ret.wager;
            wg.scale.set(1);
            if(wg.width>=w/2*0.80){
                wg.scale.set((1-(wg.width-w/2)/wg.width)*0.8);
            }
            wg.x = (ret.width/2);
            wg.y = (ret.height/2);
        };
        ret.position = function(sc,x,y){
            ctn.scale.set(sc);
            ctn.x = x;
            ctn.y = y;
        };
        ret.plus.button.command = function(){
            if(ret.wager+ret.min>ret.max){
                ret.wager = ret.max;
            }
            else{
                ret.wager +=ret.min;
            }
            ret.set();
        };
        ret.minus.button.command = function(){
            if(ret.wager-ret.min<ret.min){
                ret.wager = ret.min;
            }
            else{
                ret.wager -=ret.min;
            }
            ret.set();
        };
        return ret;
    };
    var _tpx_label_list = function(w,n){
        let ctn = new PIXI.Container();
        let ret ={};
        ret.container = ctn;
        ret.width = 0;
        ret.height = 0;
        let _list =[];
        ret.list =_list;
        let ix;
        let x = 0;
        let mx = n;
        for(ix=0;ix<n;ix++){
            let cl = _tpx_label(w,'0');
            cl.frame.tint = Math.random()*0xffffff;
            cl.container.x = x+ix*cl.width;
            ctn.addChild(cl.container);
            ret.width = cl.container.x + cl.width;
            ret.height = cl.height;
            x += 2;
            ret.list.push(cl);
        }
        ret.position = function(sc,x,y){
            ctn.scale.set(sc);
            ctn.x = x;
            ctn.y = y;
        };
        ret.set = function(n){
            for(ix=0;ix<mx;ix++){
                if(ix<n){
                    _list[ix].container.visible = true;
                }
                else{
                    _list[ix].container.visible = false;
                }
            }
        };
        ret.post = function(p){
            for(ix=mx-1;ix>=0;ix--){
                if(ix>0){
                    _list[ix].label.text = _list[ix-1].label.text;
                }
                else{
                    _list[ix].label.text = p;
                }
                _list[ix].frame.tint = Math.random()*0xffffff;
            }
        };
        return ret;
    };
    var _tpx_label = function(w,title){
        const ctn = new PIXI.Container();
        const h = w;
        const ret ={};
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        const frm  = _tpx_base_v5({w:w,h:h,b:2,r:8,t:'0x003300'});
        ctn.addChild(frm.base);
        ret.frame = frm.base;
        const t = new PIXI.Text(title,{fontFamily:_oswaldFamily,fontSize:h/2,fill:'yellow'});
        t.anchor.set(0.5);
        t.x = (ret.width/2);
        t.y = (ret.height/2);
        ret.label = t;
        ctn.addChild(t);
        return ret;
    };
    var _tpx_bar = function(w,h){
        let ret ={width:w,height:h};
        let ctn = new PIXI.Container();
        ret.container = ctn;
        const p = _tpx_base_v5({w:w,h:h,b:2,r:8,t:'0x0B3223',a:0.7});
        ctn.addChild(p.base);
        ret.position = function(sc,x,y){
            ctn.scale.set(sc);
            ctn.x = x;
            ctn.y = y;
        };
        return ret;
    };
    var _tpx_text = function(title,style){
        let ret ={};
        let ctn = new PIXI.Container();
        ret.container = ctn;
        let t = new PIXI.Text(title,style);
        ctn.addChild(t);
        ret.title = function(nt){
            t.text = nt;
            ret.width = t.width;
            ret.height = t.height;
        };
        ret.width = t.width;
        ret.height = t.height;
        ret.position = function(sc,x,y){
            ctn.scale.set(sc);
            ctn.x = x;
            ctn.y = y;
        };
        return ret;
    };
     var _tpx_dice_roll = function(){
        let ctn = new PIXI.Container();
        let w = 160;
        let h = 96;
        let ret ={};
        ret.width = w;
        ret.height = h;
        ret.container = ctn;
        const frm=_tpx_base_v5({w:w,h:h,r:8,b:4});
        frm.set({cursor:'pointer',interactive:true});
        ctn.addChild(frm.base);
        ret.button = frm;
        let d1 = new PIXI.Sprite(PIXI.Texture.WHITE);
        d1.width = 64;
        d1.height = 64;
        d1.x = 12;
        d1.y = (h-64)/2;
        ctn.addChild(d1);
        let d2 = new PIXI.Sprite(PIXI.Texture.WHITE);
        d2.width = 64;
        d2.height = 64;
        d2.x = w-64-12;
        d2.y = (h-64)/2;
        ctn.addChild(d2);
        ret.diceSet = [d1,d2];
        return ret;
    };
    var _tpx_dealer = function(){
        let ctn = new PIXI.Container();
         let ret ={};
         let w = 300;
         let h = 120;
         ret.command = function(){};
         ret.container = ctn;
         let p = new PIXI.Graphics();
         p.beginFill(0xbf40bf,1);
         p.lineStyle(2,0x000000,0.7);
         p.moveTo(h/2,0);
         p.lineTo(h/2+w,0);
         p.arc(h/2+w,h/2,h/2,Math.PI*1.5,Math.PI/2);
         p.lineTo(h/2,h);
         p.arc(h/2,h/2,h/2,Math.PI/2,Math.PI*1.5);
         p.endFill();
         ret.background = p;
         ctn.addChild(p);

         let ic = _tpx_icon_command(60,60);
         ic.container.x = h/2+30;
         ic.container.y = (h-60)/5;
         ret.avatar = ic;
         let nm = new PIXI.Text('Live Dealer Name',{fontWeight:500,fontFamily:_oswaldFamily,fontSize: 32,fill: "white",});
         nm.x = ic.container.x+70;
         nm.y = (h/2-nm.height);
         ret.name = function(n){
            nm.text = n;
             nm.scale.set(1);
            if(nm.width>=(w)*1){
                nm.scale.set((1-(nm.width-(w))/nm.width)*1);
            }
            nm.x = ic.container.x+70;
            nm.y = (h/2-nm.height);
         };
         let tx = new PIXI.Text(0,{fontWeight:500,fontFamily:_oswaldFamily,fontSize: 32,fill: "yellow",});
         tx.x = (w+h-tx.width)/2;
         tx.y = (h-tx.height-4);
         ctn.addChild(tx);
         ret.balance = function(tn){
            tx.text = tn;
            //tx.scale.set(1);
            //if(tx.width>=(w-60)*0.80){
                //tx.scale.set((1-(tx.width-(w-60))/tx.width)*0.8);
            //}
            tx.x = (w+h-tx.width)/2;
            tx.y = (h-tx.height-6);
         }
         const pl = _tpx_base_v5({w:(w+h*1.5),h:h/2,r:16,b:2,t:'0x000000',a:'0.4'});
         pl.set({cursor:'pointer',interactive:true});
         pl.up=()=>{};
         pl.out=()=>{};
         pl.over=()=>{pl.base.alpha=0.5;};
         pl.base.x = -30;
         pl.base.y = (h-pl.height)/5;
         ctn.addChild(pl.base);
         ctn.addChild(nm);
         ctn.addChild(ic.container);
         let st = new PIXI.Text('Sit Down',{fontWeight:500,fontFamily: _oswaldFamily,fontSize:30,fill: "yellow",});
         st.x = -24;
         st.y = (h-st.height)/3;
         ctn.addChild(st);
         ret.width = w+h;
         ret.height = h;
         ret.title = st;
         ret.seatBox = pl;
         ret.position = function(s,x,y){ctn.scale.set(s);ctn.x = x;ctn.y = y;};
         ret.handList=[];

         ret.amount = 0;
         ret.amountBase =0;
         ret.start = 0;
         ret.duration = 1000;
         ret.update = function(nb){
             requestAnimationFrame(function(tm){
                 ret.start = tm;
                 ret.amountBase = ret.amount;
                 ret.amount = nb;
                 requestAnimationFrame(ret.frame);
             });
         };
         ret.frame = function(tm){
             let ds = tm - ret.start;
             if(ds<ret.duration){
                 let delta = ((ret.amount-ret.amountBase)*(ds/ret.duration)).toFixed(2);
                 if(delta>0){
                     delta = (parseFloat(delta) + parseFloat(ret.amountBase)).toFixed(2);
                 }
                 else{
                     delta = (ret.amountBase-Math.abs(delta)).toFixed(2);
                 }
                 ret.balance(delta);
                 requestAnimationFrame(ret.frame);
             }
             else{
                 ret.balance(ret.amount);
             }
         };

         return ret;
     };
     var _tpx_stat_board = function(n){
        	let ctn = new PIXI.Container();
            let ret ={};
            ret.command = function(){ctn.visible=false;};
            ret.width = 650;
            ret.height = 80*(n+1);
            ret.container = ctn;
        	let p = new PIXI.Graphics();
            p.beginFill(0x000000,0.7);
            p.lineStyle(0,0x000000,1);
           	p.drawRoundedRect(0,0,ret.width,ret.height,8);
            p.endFill();
            //let pl = new PIXI.Sprite(p.generateCanvasTexture());
            p.alpha = 1;
            p.interactive = true;
            p.buttonMode = true;
            p.on('pointerover',function(){
                //pl.alpha = 0.5;
            });
            p.on('pointerout',function(){
                //pl.alpha = 1;
            });
            p.on('pointerdown',function(e){
                ret.command(e);
            });
            ctn.addChild(p);

            let ix;
            let iy = 0;
            let _lines =[];
            for(ix=0;ix<n+1;ix++){
            	let r = _tpx_stat_line(650,80);
                r.container.y = iy;
            	iy +=r.height;
                if(ix>0){
                	r.set('--','--','--');
                }
                r.frame.tint = Math.random()*0xffffff;
                ctn.addChild(r.container);
            	_lines.push(r);
            }
            ret.lines = _lines;
            ret.position = function(sc,x,y){ctn.scale.set(sc),ctn.x = x; ctn.y = y;};
            return ret;
        };
     var _tpx_stat_line = function(w,h){
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
         ret.frame = p;
         ctn.addChild(p);
         let t1 = new PIXI.Text('##',{fontWeight: '500',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
         t1.x = 16
         t1.y = (h-t1.height)/2;
         ctn.addChild(t1);
         let t2 = new PIXI.Text('NAME',{fontWeight: '500',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
         t2.x = t1.x + t1.width+16;
         t2.y = (h-t2.height)/2;
         ctn.addChild(t2);
         let t3 = new PIXI.Text('COUNT',{fontWeight: '500',fontFamily: _oswaldFamily,fontSize:32,fill:'yellow'});
         t3.x = t2.x+t2.width+200;
         t3.y = (h-t3.height)/2;
         ctn.addChild(t3);
         ret.set = function(r,s,c){t1.text = r; t2.text=s; t3.text=c;p.tint=Math.random()*0xffffff;};
         ret.position = function(sc,x,y){ctn.scale.set(sc),ctn.x = x; ctn.y = y;};
         return ret;
     };
     var _tpx_lobby_setup = function(){
         const ret ={};
         ret.width = 360;
         ret.height = 500;
         const ctn = new PIXI.Container();
         const bk = _tpx_base_v5({w:ret.width,h:ret.height,r:20,b:8,t:'0x000000',a:'1'});
         ctn.addChild(bk.base);
         const t = _tpx_text_v5(500,30,'Oswald','yellow');
         ctn.addChild(t);
         t.x = 10;
         t.y = 10;
         const cmd = _tpx_text_command(160,80,'Lobby');
         ctn.addChild(cmd.container);
         cmd.container.x = ret.width-cmd.width-30;
         cmd.container.y = ret.height-cmd.height-30;
         ret.container = ctn;
         ret.setup = cmd;
         ret.set = function(tx){t.text = tx;};
         ret.position = function(sc,x,y){ctn.scale.set(sc),ctn.x = x; ctn.y = y;};
         return ret;
    };
    return {
        cashInView : _tpx_cashin_view,
        balanceView : _tpx_balance_view,
        applicationShortcut : _tpx_application_shortcut,
        applicationList : _tpx_application_list,
        textCommand : _tpx_text_command,
        textCommandList : _tpx_command_list,
        iconCommand : _tpx_icon_command,
        confirmation : _tpx_confirmation,
        wagerBar : _tpx_wager_bar,
        label : _tpx_label,
        labelList : _tpx_label_list,
        text : _tpx_text,
        bar : _tpx_bar,
        menu : _tpx_menu,
        exit : _tpx_exit,
        diceRoll : _tpx_dice_roll,
        dealer : _tpx_dealer,
        statisticsBoard : _tpx_stat_board,
        avatar : _tpx_avatar,
        lobbySetup : _tpx_lobby_setup,
    };
})();
