package cl.myconstruction;

public class Main {

    public static void main(String[] args) {

        try {

            System.out.println("===============================");
            System.out.println("       MY CONSTRUCTION");
            System.out.println("===============================");

            WebServer.iniciar();

        } catch (Exception e) {

            System.out.println(
                    "No fue posible iniciar MyConstruction.");

            e.printStackTrace();
        }
    }
}