class TicTacToe{
    #name;
    #score;
    constructor(name){
        this.#name = name;
        this.#score=100;    
    }
    show_name() {
        console.log(this.#name+"><"+this.#score);
    }
}