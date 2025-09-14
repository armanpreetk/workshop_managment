import { useEffect, useState } from 'react';
import { api, useAuth } from '../auth/AuthContext';
import Card from '../components/Card';
import Input from '../components/Input';
import Button from '../components/Button';
import Table, { TableRow, TableCell } from '../components/Table';
import Alert from '../components/Alert';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import Select from '../components/Select';
import Navbar from '../components/Navbar';

type Manager = {
  id: number;
  name: string;
  email: string;
  password?: string;
};

type Employee = {
  id: number;
  name: string;
  email: string;
  password?: string;
};

export default function AdminDashboard() {
  const { user, logout } = useAuth();
  const [managers, setManagers] = useState<Manager[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [newManager, setNewManager] = useState<Manager>({ id: 0, name: '', email: '', password: '' });
  const [newEmployee, setNewEmployee] = useState<Employee>({ id: 0, name: '', email: '', password: '' });
  const [selectedManagerId, setSelectedManagerId] = useState<number | ''>('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [apiError, setApiError] = useState<string | null>(null);
  const [apiSuccess, setApiSuccess] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  
  // Modal states
  const [managerToEdit, setManagerToEdit] = useState<Manager | null>(null);
  const [employeeToEdit, setEmployeeToEdit] = useState<Employee | null>(null);
  const [managerToDelete, setManagerToDelete] = useState<Manager | null>(null);
  const [employeeToDelete, setEmployeeToDelete] = useState<Employee | null>(null);

  // Helper function to process error messages
  const handleApiError = (err: any, defaultMessage: string) => {
    const errorMessage = err?.response?.data?.message || defaultMessage;
    
    // Extract the most useful part of the message if it's too technical
    let userFriendlyMessage = errorMessage;
    
    if (errorMessage.includes('already in use') || errorMessage.includes('already exists')) {
      // Already user-friendly from backend
      userFriendlyMessage = errorMessage;
    } else if (errorMessage.includes('Duplicate entry')) {
      if (errorMessage.toLowerCase().includes('email')) {
        userFriendlyMessage = 'This email is already registered. Please use a different email address.';
      } else {
        userFriendlyMessage = 'This information is already in use. Please check your input.';
      }
    } else if (errorMessage.includes('constraint')) {
      userFriendlyMessage = 'Unable to complete this action due to database constraints.';
    } else if (errorMessage.includes('foreign key')) {
      userFriendlyMessage = 'This record is linked to other data and cannot be modified.';
    }
    
    setApiError(userFriendlyMessage);
    if (err?.response?.data?.errors) {
      setErrors(err.response.data.errors);
    }
  };

  const load = async () => {
    try {
      setIsLoading(true);
      const [managersRes, employeesRes] = await Promise.all([
        api.get('/api/managers'),
        api.get('/api/employees')
      ]);
      setManagers(managersRes.data);
      setEmployees(employeesRes.data);
      setApiError(null);
    } catch (err: any) {
      handleApiError(err, 'Failed to load data');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => { 
    load();
  }, []);

  const validateManager = (manager: Manager) => {
    const newErrors: Record<string, string> = {};
    
    if (!manager.name) newErrors.name = 'Name is required';
    if (!manager.email) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(manager.email)) {
      newErrors.email = 'Email is invalid';
    }
    if (!manager.password) newErrors.password = 'Password is required';
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateEmployee = (employee: Employee) => {
    const newErrors: Record<string, string> = {};
    
    if (!employee.name) newErrors.name = 'Name is required';
    if (!employee.email) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(employee.email)) {
      newErrors.email = 'Email is invalid';
    }
    if (!employee.password) newErrors.password = 'Password is required';
    if (selectedManagerId === '') newErrors.managerId = 'Manager is required';
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const createManager = async () => {
    if (!validateManager(newManager)) return;
    
    try {
      setIsLoading(true);
      await api.post('/api/managers', newManager);
      setNewManager({ id: 0, name: '', email: '', password: '' });
      setApiSuccess('Manager created successfully');
      setTimeout(() => {
        setApiSuccess(null);
        window.location.reload(); // Refresh the page
      }, 1000);
      await load();
    } catch (err: any) {
      handleApiError(err, 'Failed to create manager');
    } finally {
      setIsLoading(false);
    }
  };

  const updateManager = async () => {
    if (!managerToEdit) return;
    if (!validateManager(managerToEdit)) return;
    
    try {
      setIsLoading(true);
      await api.put(`/api/managers/${managerToEdit.id}`, managerToEdit);
      setManagerToEdit(null);
      setApiSuccess('Manager updated successfully');
      setTimeout(() => {
        setApiSuccess(null);
        window.location.reload(); // Refresh the page
      }, 1000);
      await load();
    } catch (err: any) {
      handleApiError(err, 'Failed to update manager');
    } finally {
      setIsLoading(false);
    }
  };

  const deleteManager = async () => {
    if (!managerToDelete) return;
    
    try {
      setIsLoading(true);
      await api.delete(`/api/managers/${managerToDelete.id}`);
      setManagerToDelete(null);
      setApiSuccess('Manager deleted successfully');
      setTimeout(() => {
        setApiSuccess(null);
        window.location.reload(); // Refresh the page
      }, 1000);
      await load();
    } catch (err: any) {
      handleApiError(err, 'Failed to delete manager');
    } finally {
      setIsLoading(false);
    }
  };

  const createEmployee = async () => {
    if (!validateEmployee(newEmployee)) return;
    
    try {
      setIsLoading(true);
      await api.post(`/api/employees?managerId=${selectedManagerId}`, newEmployee);
      setNewEmployee({ id: 0, name: '', email: '', password: '' });
      setSelectedManagerId('');
      setApiSuccess('Employee created successfully');
      setTimeout(() => {
        setApiSuccess(null);
        window.location.reload(); // Refresh the page
      }, 1000);
      await load();
    } catch (err: any) {
      handleApiError(err, 'Failed to create employee');
    } finally {
      setIsLoading(false);
    }
  };

  const updateEmployee = async () => {
    if (!employeeToEdit) return;
    if (!validateManager(employeeToEdit)) return;
    
    try {
      setIsLoading(true);
      await api.put(`/api/employees/${employeeToEdit.id}`, employeeToEdit);
      setEmployeeToEdit(null);
      setApiSuccess('Employee updated successfully');
      setTimeout(() => {
        setApiSuccess(null);
        window.location.reload(); // Refresh the page
      }, 1000);
      await load();
    } catch (err: any) {
      handleApiError(err, 'Failed to update employee');
    } finally {
      setIsLoading(false);
    }
  };

  const deleteEmployee = async () => {
    if (!employeeToDelete) return;
    
    try {
      setIsLoading(true);
      await api.delete(`/api/employees/${employeeToDelete.id}`);
      setEmployeeToDelete(null);
      setApiSuccess('Employee deleted successfully');
      setTimeout(() => {
        setApiSuccess(null);
        window.location.reload(); // Refresh the page
      }, 1000);
      await load();
    } catch (err: any) {
      handleApiError(err, 'Failed to delete employee');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar user={user} onLogout={logout} />
      
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {apiError && (
          <Alert 
            type="error" 
            message={apiError} 
            onDismiss={() => setApiError(null)} 
          />
        )}
        
        {apiSuccess && (
          <Alert 
            type="success" 
            message={apiSuccess} 
            onDismiss={() => setApiSuccess(null)} 
          />
        )}
        
        {/* Managers Section */}
        <div className="mb-8">
          <Card 
            title="Managers"
            className="mb-4"
          >
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
              <Input
                label="Name"
                placeholder="Manager name"
                value={newManager.name}
                onChange={(e) => setNewManager({...newManager, name: e.target.value})}
                error={errors.name}
                fullWidth
              />
              <Input
                label="Email"
                placeholder="email@example.com"
                type="email"
                value={newManager.email}
                onChange={(e) => setNewManager({...newManager, email: e.target.value})}
                error={errors.email}
                fullWidth
              />
              <Input
                label="Password"
                placeholder="Password"
                type="password"
                value={newManager.password || ''}
                onChange={(e) => setNewManager({...newManager, password: e.target.value})}
                error={errors.password}
                fullWidth
              />
            </div>
            <div className="flex justify-end">
              <Button
                variant="primary"
                onClick={createManager}
                isLoading={isLoading}
              >
                Create Manager
              </Button>
            </div>
          </Card>
          
          <Card>
            <Table headers={['Name', 'Email', 'Actions']}>
              {managers.map((manager) => (
                <TableRow key={manager.id}>
                  <TableCell>{manager.name}</TableCell>
                  <TableCell>{manager.email}</TableCell>
                  <TableCell>
                    <div className="flex space-x-2">
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => setManagerToEdit({...manager, password: ''})}
                      >
                        Edit
                      </Button>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => setManagerToDelete(manager)}
                      >
                        Delete
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
              {managers.length === 0 && (
                <TableRow>
                  <TableCell colSpan={3} className="text-center text-gray-500">
                    No managers found
                  </TableCell>
                </TableRow>
              )}
            </Table>
          </Card>
        </div>
        
        {/* Employees Section */}
        <div>
          <Card 
            title="Employees"
            className="mb-4"
          >
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
              <Input
                label="Name"
                placeholder="Employee name"
                value={newEmployee.name}
                onChange={(e) => setNewEmployee({...newEmployee, name: e.target.value})}
                error={errors.name}
                fullWidth
              />
              <Input
                label="Email"
                placeholder="email@example.com"
                type="email"
                value={newEmployee.email}
                onChange={(e) => setNewEmployee({...newEmployee, email: e.target.value})}
                error={errors.email}
                fullWidth
              />
              <Input
                label="Password"
                placeholder="Password"
                type="password"
                value={newEmployee.password || ''}
                onChange={(e) => setNewEmployee({...newEmployee, password: e.target.value})}
                error={errors.password}
                fullWidth
              />
              <Select
                label="Manager"
                value={selectedManagerId}
                onChange={(e) => setSelectedManagerId(e.target.value ? Number(e.target.value) : '')}
                options={[
                  { value: '', label: 'Select Manager' },
                  ...managers.map(manager => ({ 
                    value: manager.id, 
                    label: `${manager.name} (${manager.email})` 
                  }))
                ]}
                error={errors.managerId}
                fullWidth
              />
            </div>
            <div className="flex justify-end">
              <Button
                variant="primary"
                onClick={createEmployee}
                isLoading={isLoading}
              >
                Create Employee
              </Button>
            </div>
          </Card>
          
          <Card>
            <Table headers={['Name', 'Email', 'Actions']}>
              {employees.map((employee) => (
                <TableRow key={employee.id}>
                  <TableCell>{employee.name}</TableCell>
                  <TableCell>{employee.email}</TableCell>
                  <TableCell>
                    <div className="flex space-x-2">
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => setEmployeeToEdit({...employee, password: ''})}
                      >
                        Edit
                      </Button>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => setEmployeeToDelete(employee)}
                      >
                        Delete
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
              {employees.length === 0 && (
                <TableRow>
                  <TableCell colSpan={3} className="text-center text-gray-500">
                    No employees found
                  </TableCell>
                </TableRow>
              )}
            </Table>
          </Card>
        </div>
      </div>
      
      {/* Manager Edit Modal */}
      <Modal
        isOpen={!!managerToEdit}
        onClose={() => setManagerToEdit(null)}
        title="Edit Manager"
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() => setManagerToEdit(null)}
              className="mr-2"
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={updateManager}
              isLoading={isLoading}
            >
              Save
            </Button>
          </>
        }
      >
        {managerToEdit && (
          <div className="space-y-4">
            <Input
              label="Name"
              value={managerToEdit.name}
              onChange={(e) => setManagerToEdit({...managerToEdit, name: e.target.value})}
              error={errors.name}
              fullWidth
            />
            <Input
              label="Email"
              type="email"
              value={managerToEdit.email}
              onChange={(e) => setManagerToEdit({...managerToEdit, email: e.target.value})}
              error={errors.email}
              fullWidth
            />
            <Input
              label="Password"
              type="password"
              value={managerToEdit.password || ''}
              onChange={(e) => setManagerToEdit({...managerToEdit, password: e.target.value})}
              error={errors.password}
              placeholder="Leave empty to keep current password"
              fullWidth
            />
          </div>
        )}
      </Modal>
      
      {/* Employee Edit Modal */}
      <Modal
        isOpen={!!employeeToEdit}
        onClose={() => setEmployeeToEdit(null)}
        title="Edit Employee"
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() => setEmployeeToEdit(null)}
              className="mr-2"
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={updateEmployee}
              isLoading={isLoading}
            >
              Save
            </Button>
          </>
        }
      >
        {employeeToEdit && (
          <div className="space-y-4">
            <Input
              label="Name"
              value={employeeToEdit.name}
              onChange={(e) => setEmployeeToEdit({...employeeToEdit, name: e.target.value})}
              error={errors.name}
              fullWidth
            />
            <Input
              label="Email"
              type="email"
              value={employeeToEdit.email}
              onChange={(e) => setEmployeeToEdit({...employeeToEdit, email: e.target.value})}
              error={errors.email}
              fullWidth
            />
            <Input
              label="Password"
              type="password"
              value={employeeToEdit.password || ''}
              onChange={(e) => setEmployeeToEdit({...employeeToEdit, password: e.target.value})}
              error={errors.password}
              placeholder="Leave empty to keep current password"
              fullWidth
            />
          </div>
        )}
      </Modal>
      
      {/* Manager Delete Confirmation */}
      <ConfirmDialog
        isOpen={!!managerToDelete}
        onClose={() => setManagerToDelete(null)}
        onConfirm={deleteManager}
        title="Delete Manager"
        message="Are you sure you want to delete this manager? This action cannot be undone."
        confirmButtonText="Delete"
        confirmButtonVariant="danger"
        isLoading={isLoading}
      />
      
      {/* Employee Delete Confirmation */}
      <ConfirmDialog
        isOpen={!!employeeToDelete}
        onClose={() => setEmployeeToDelete(null)}
        onConfirm={deleteEmployee}
        title="Delete Employee"
        message="Are you sure you want to delete this employee? This action cannot be undone."
        confirmButtonText="Delete"
        confirmButtonVariant="danger"
        isLoading={isLoading}
      />
    </div>
  )
}
