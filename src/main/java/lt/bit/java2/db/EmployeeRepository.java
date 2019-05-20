package lt.bit.java2.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lt.bit.java2.Main;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmployeeRepository {

    public Employee get(Integer empNo) throws IOException {
        DataSource dataSource = DBUtils.getDataSource();

        Connection connection = null;

        try {
            connection = dataSource.getConnection();

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT e.emp_no, first_name, last_name, hire_date, birth_date, gender, from_date, to_date, salary  " +
                            " FROM employees e left join salaries s on e.emp_no = s.emp_no " +
                            " WHERE e.emp_no = ?");

            statement.setInt(1, empNo);

            Employee employee = null;

            //TODO - nebaigta - reikia kazkaip nuskaityti Salary info ir sudeti i List<Salary>
            
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                 employee = new Employee(resultSet.getInt("emp_no"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getDate("birth_date").toLocalDate(),
                        resultSet.getDate("hire_date").toLocalDate(),
                        Gender.fromValue(resultSet.getString("gender")));
            }

            statement.close();

            connection.close();

            return employee;

        } catch (SQLException e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return null;
    }

    public void create(Employee employee) throws IOException {
        DataSource dataSource = DBUtils.getDataSource();

        Connection connection = null;

        try {
            connection = dataSource.getConnection();

            // Pradedame tranzakcija
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO employees(emp_no, birth_date, first_name, last_name, gender, hire_date) " +
                            " VALUES(?,?,?,?,?,?)");

            statement.setInt(1, employee.getEmpNo());
            statement.setDate(2, Date.valueOf(employee.getBirthDate()));
            statement.setString(3, employee.getFirstName());
            statement.setString(4, employee.getLastName());
            if (employee.getGender() != null) statement.setString(5, employee.getGender().getValue());
            statement.setDate(6, Date.valueOf(employee.getHireDate()));

            statement.execute();

            statement.close();

            // uzbaigiame tranzakcija:
            connection.commit();

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public List<Employee> list(String name, int pageSize, int offset) throws IOException {
        if (offset < 0) offset = 0;
        if (pageSize < 5 || pageSize > 100) pageSize = 5;

        List<Employee> employees = null;

        DataSource dataSource = DBUtils.getDataSource();

        Connection connection = null;

        try {
            connection = dataSource.getConnection();

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT emp_no, first_name, last_name, hire_date, birth_date, gender " +
                            " FROM employees " +
                            " WHERE first_name LIKE ?" +
                            " ORDER BY emp_no " +
                            " LIMIT ? OFFSET ?");

            statement.setString(1, "%" + name + "%");
            statement.setInt(2, pageSize);
            statement.setInt(3, offset);

            ResultSet resultSet = statement.executeQuery();

            List<Employee> result = new ArrayList<>();

            while (resultSet.next()) {

                Employee employee = new Employee(resultSet.getInt("emp_no"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getDate("birth_date").toLocalDate(),
                        resultSet.getDate("hire_date").toLocalDate(),
                        Gender.fromValue(resultSet.getString("gender")));

                result.add(employee);
            }

            statement.close();
            connection.close();

            return result;


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
