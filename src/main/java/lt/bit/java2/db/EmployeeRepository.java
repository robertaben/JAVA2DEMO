package lt.bit.java2.db;

import lt.bit.java2.db.entities.Employee;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.io.IOException;
import java.util.List;

public class EmployeeRepository {

    public Employee get(Integer empNo) {
        EntityManager em = DBUtils.getEntityManager();
        return em.find(Employee.class, empNo);
    }

    public void create(Employee employee) {
        DBUtils.executeInTransaction(em -> {
            em.persist(employee);
            return employee;
        });
    }

    public void delete(Integer empNo) {
        DBUtils.executeInTransaction(em -> {
            Employee employee = em.find(Employee.class, empNo);
            if (employee == null) throw new PersistenceException("Entity not found");
            em.remove(employee);
            return true;
        });
    }

    public List<Employee> list(String name, int pageSize, int offset) {
        //TODO
        return null;
    }

}
