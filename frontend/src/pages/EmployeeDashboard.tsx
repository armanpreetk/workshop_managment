import { useEffect, useState } from 'react';
import { api, useAuth } from '../auth/AuthContext';
import Card from '../components/Card';
import Button from '../components/Button';
import Table, { TableRow, TableCell } from '../components/Table';
import Alert from '../components/Alert';
import Navbar from '../components/Navbar';

type Employee = {
  id: number;
  name: string;
  email: string;
};

export default function EmployeeDashboard() {
  const { user, logout } = useAuth();
  const [profile, setProfile] = useState<Employee | null>(null);
  const [team, setTeam] = useState<Employee[]>([]);
  const [apiError, setApiError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  type Task = { id: number; title: string; description?: string; status: 'PENDING'|'DONE'; dueDate?: string };
  const [tasks, setTasks] = useState<Task[]>([]);

  // Helper function to process error messages
  const handleApiError = (err: any, defaultMessage: string) => {
    const errorMessage = err?.response?.data?.message || defaultMessage;
    
    // Extract the most useful part of the message if it's too technical
    let userFriendlyMessage = errorMessage;
    
    if (errorMessage.includes('already in use') || errorMessage.includes('already exists')) {
      // Already user-friendly from backend
      userFriendlyMessage = errorMessage;
    } else if (errorMessage.includes('Duplicate entry')) {
      userFriendlyMessage = 'This information is already in use. Please check your input.';
    } else if (errorMessage.includes('constraint')) {
      userFriendlyMessage = 'Unable to complete this action due to database constraints.';
    } else if (errorMessage.includes('foreign key')) {
      userFriendlyMessage = 'This record is linked to other data and cannot be modified.';
    }
    
    setApiError(userFriendlyMessage);
  };

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [profileRes, teamRes] = await Promise.all([
        api.get('/api/employees/me/profile'),
        api.get('/api/employees/me/team')
      ]);
      setProfile(profileRes.data);
      setTeam(teamRes.data);
      setApiError(null);
    } catch (err: any) {
      handleApiError(err, 'Failed to load data');
    } finally {
      setIsLoading(false);
    }
  };

  const loadTasks = async () => {
    try {
      const res = await api.get('/api/tasks/employee/me');
      setTasks(res.data);
    } catch {}
  };

  useEffect(() => { loadData(); loadTasks(); }, []);

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
        
        {/* Profile */}
        <div className="mb-8">
          <Card title="My Profile" className="mb-6">
            {profile ? (
              <div className="bg-white rounded-lg overflow-hidden shadow">
                <div className="px-4 py-5 sm:px-6 bg-gray-50">
                  <h3 className="text-lg leading-6 font-medium text-gray-900">
                    Employee Information
                  </h3>
                  <p className="mt-1 max-w-2xl text-sm text-gray-500">
                    Personal details and team information.
                  </p>
                </div>
                <div className="border-t border-gray-200">
                  <dl>
                    <div className="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                      <dt className="text-sm font-medium text-gray-500">
                        Full name
                      </dt>
                      <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                        {profile.name}
                      </dd>
                    </div>
                    <div className="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                      <dt className="text-sm font-medium text-gray-500">
                        Email address
                      </dt>
                      <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                        {profile.email}
                      </dd>
                    </div>
                    <div className="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                      <dt className="text-sm font-medium text-gray-500">
                        Role
                      </dt>
                      <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                        Employee
                      </dd>
                    </div>
                  </dl>
                </div>
              </div>
            ) : (
              <div className="text-center py-4 text-gray-500">
                {isLoading ? 'Loading profile...' : 'Profile not found'}
              </div>
            )}
          </Card>
        </div>
        
        {/* Tasks */}
        <Card title="My Tasks" className="mb-8">
          <Table headers={["Title", "Status", "Due Date", "Actions"]}>
            {tasks.map(t => (
              <TableRow key={t.id}>
                <TableCell>{t.title}</TableCell>
                <TableCell>{t.status}</TableCell>
                <TableCell>{t.dueDate || '-'}</TableCell>
                <TableCell>
                  {t.status === 'PENDING' && (
                    <Button variant="primary" size="sm" onClick={async () => {
                      try {
                        await api.post(`/api/tasks/${t.id}/done`);
                        await loadTasks();
                      } catch (err: any) {
                        setApiError(err?.response?.data?.message || 'Failed to complete task');
                      }
                    }}>Mark done</Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
            {tasks.length === 0 && (
              <TableRow>
                <TableCell colSpan={4} className="text-center text-gray-500">No tasks assigned</TableCell>
              </TableRow>
            )}
          </Table>
        </Card>

        {/* Team */}
        <Card title="My Team">
          <p className="text-gray-600 mb-4">
            You are on a team with {team.length} other employee{team.length !== 1 ? 's' : ''}.
          </p>
          
          <Table headers={['Name', 'Email']}>
            {team.map((employee) => (
              <TableRow key={employee.id}>
                <TableCell>{employee.name}</TableCell>
                <TableCell>{employee.email}</TableCell>
              </TableRow>
            ))}
            {team.length === 0 && (
              <TableRow>
                <TableCell colSpan={2} className="text-center text-gray-500">
                  No team members found.
                </TableCell>
              </TableRow>
            )}
          </Table>
        </Card>
      </div>
    </div>
  );
}
