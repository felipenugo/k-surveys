package edu.upc.prop.clusterxx;

import java.util.Scanner;

public class DriverCtrlDominioUsuario {
    public static void main(String[] args) {
        CtrlDominioUsuario ctrlDominioUsuario = new CtrlDominioUsuario();
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        boolean sesion = false;
        while (!exit) {
            while(!sesion && !exit) {
                imprimirMenu("Main");
                String input = scanner.nextLine();
                if (input.equals("REGISTRAR")) {
                    System.out.println("Introduzca [Usuario] [Contraseña] [Contraseña] ");
                    String linea = scanner.nextLine();
                    String[] datos = linea.split(" ");
                    if (datos.length < 3) {
                        System.out.println("Formato incorrecto.\n");
                    }
                    try {
                        if (ctrlDominioUsuario.registrarUsuario(datos[0], datos[1], datos[2]))
                            System.out.println("Usuario registrado con éxito.\n");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error en los parámetros: " + e.getMessage());
                    }
                }
                else if (input.equals("LOG_IN")) {
                    System.out.println("Introduzca [Usuario] [Contraseña] ");
                    String linea = scanner.nextLine();
                    String[] datos = linea.split(" ");
                    if (datos.length < 2) {
                        System.out.println("Formato incorrecto.\n");
                        continue;
                    }
                    try {
                        if (ctrlDominioUsuario.iniciarSesion(datos[0], datos[1])) {
                            System.out.println("Sesión iniciada.\n");
                            sesion = true;
                        }
                        else {
                            System.out.println("Nombre o contraseña incorrectos.\n");
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error en los parametros: " + e.getMessage());
                    }
                }
                else if(input.equals("EXIT")) exit = true;
                else System.out.println("Introduzca un comando valido.\n");
            }
            while(sesion) {
                imprimirMenu("otro");
                String input = scanner.nextLine();
                if (input.equals("LOG_OUT")) {
                    ctrlDominioUsuario.cerrarSesion();
                    sesion = false;
                }
                else System.out.println("Introduzca un comando valido.\n");
            }
        }
        System.out.println("Saliendo de la aplicación.");
    }
    private static void imprimirMenu(String menu) {
        if(menu.equals("Main")) {
            System.out.println("REGISTRAR - Registrar usuario.");
            System.out.println("LOG_IN - Iniciar sesión.");
            System.out.println("EXIT - Salir de la aplicacion.");
            System.out.println();
        }
        else {
            System.out.println("LOG_OUT - Cerrar sesión.");
            System.out.println();
        }
    }
}
