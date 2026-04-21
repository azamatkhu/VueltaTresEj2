import java.sql.*;

public class MainVueltaTresEj2 {
    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe",
                "RIBERA",
                "ribera"
        )) {
            System.out.println("Conectado!");

            String sqlTopCinco = "SELECT c.NOMBRE, c.NACIONALIDAD, e.NOMBRE AS NOMBRE_EQUIPO, SUM(p.PUNTOS) AS PUNTOS_TOTAL, ROUND(AVG(p.PUNTOS), 2) AS PUNTOS_PROMEDIO, COUNT(p.NUMERO_ETAPA) AS ETAPAS_DISPUTADAS\n" +
                    "FROM CICLISTA c JOIN EQUIPO e ON c.ID_EQUIPO = e.ID_EQUIPO\n" +
                    "JOIN PARTICIPACION p ON p.ID_CICLISTA = c.ID_CICLISTA\n" +
                    "GROUP BY c.NOMBRE, c.NACIONALIDAD, e.NOMBRE\n" +
                    "ORDER BY PUNTOS_TOTAL DESC, PUNTOS_PROMEDIO DESC";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlTopCinco);
            int contador = 1;

            System.out.println("----------- ESTADISTICAS -----------");
            System.out.println("-- Top 5 ciclistas con mejor rendimiento: ");
            while (contador <= 5) {
                resultSet.next();
                System.out.println("- " + resultSet.getString(1) + " - " + resultSet.getString(2) + " - " + resultSet.getString(3) + " - " + resultSet.getString(4) + " - " + resultSet.getString(5) + " - " + resultSet.getString(6));
                contador++;
            }

            System.out.println(" ");

            String sqlEquipos = "SELECT e.NOMBRE, e.PAIS, COUNT(c.ID_CICLISTA) AS NUMERO_CICLISTAS, AVG(c.EDAD) AS MEDIA_EDAD,\n" +
                    "    (SELECT NOMBRE FROM CICLISTA JOIN PARTICIPACION USING(ID_CICLISTA) WHERE ID_EQUIPO = e.ID_EQUIPO GROUP BY NOMBRE ORDER BY SUM(PUNTOS) DESC FETCH FIRST 1 ROWS ONLY) AS MEJOR_CICLISTA\n" +
                    "FROM EQUIPO e JOIN CICLISTA c ON e.ID_EQUIPO = c.ID_EQUIPO\n" +
                    "GROUP BY e.ID_EQUIPO, e.NOMBRE, e.PAIS";

            System.out.println("-- Comparativa de equipos: ");
            resultSet = statement.executeQuery(sqlEquipos);
            while (resultSet.next()) {
                System.out.println("- " + resultSet.getString(1) + " - " + resultSet.getString(2) + " - " + resultSet.getString(3) + " - " + resultSet.getString(4) + " - " + resultSet.getString(5));
            }

            System.out.println(" ");

            String sqlEtapa = "SELECT e.NUMERO, e.ORIGEN, e.DESTINO, e.FECHA, e.DISTANCIA_KM, p.ID_CICLISTA, p.PUNTOS\n" +
                    "FROM ETAPA e JOIN PARTICIPACION p ON e.NUMERO = p.NUMERO_ETAPA\n" +
                    "WHERE DISTANCIA_KM > (SELECT AVG(DISTANCIA_KM) FROM ETAPA) OR\n" +
                    "    DISTANCIA_KM = (SELECT MAX(DISTANCIA_KM) FROM ETAPA) OR \n" +
                    "    DISTANCIA_KM = (SELECT MIN(DISTANCIA_KM) FROM ETAPA) OR\n" +
                    "    NUMERO_ETAPA IN (\n" +
                    "            SELECT NUMERO_ETAPA FROM PARTICIPACION WHERE PUNTOS > 0 GROUP BY NUMERO_ETAPA HAVING COUNT(ID_CICLISTA) > 10\n" +
                    "    )\n" +
                    "GROUP BY e.NUMERO, e.ORIGEN, e.DESTINO, e.FECHA, e.DISTANCIA_KM, p.ID_CICLISTA, p.PUNTOS";

            System.out.println("-- Etapas especiales");
            resultSet = statement.executeQuery(sqlEtapa);
            while (resultSet.next()) {
                System.out.println("- " + resultSet.getString(1) + " - " + resultSet.getString(2) + " -> " + resultSet.getString(3) + " - " + resultSet.getString(4) + " - " + resultSet.getString(5) + " - " + resultSet.getString(6) + " - " + resultSet.getString(7));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}