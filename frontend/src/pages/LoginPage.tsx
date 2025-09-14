import { FormEvent, useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { useNavigate } from 'react-router-dom';
import Card from '../components/Card';
import Input from '../components/Input';
import Button from '../components/Button';
import Alert from '../components/Alert';

export default function LoginPage() {
  const { login, user } = useAuth();
  const nav = useNavigate();
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    
    if (!name || !password) {
      setError('Please enter both name and password');
      return;
    }
    
    try {
      setIsLoading(true);
      const u = await login(name, password);
      if (u.type === 'ADMIN') nav('/admin');
      else if (u.type === 'MANAGER') nav('/manager');
      else if (u.type === 'EMPLOYEE') nav('/employee');
      else nav('/');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Login failed. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Workshop Management
        </h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          Sign in to your account
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          {error && (
            <Alert 
              type="error" 
              message={error} 
              onDismiss={() => setError(null)} 
              className="mb-4"
            />
          )}
          
          <form className="space-y-6" onSubmit={onSubmit}>
            <Input
              label="Username"
              id="name"
              name="name"
              type="text"
              autoComplete="username"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              fullWidth
            />

            <Input
              label="Password"
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              fullWidth
            />

            <div>
              <Button
                type="submit"
                variant="primary"
                isLoading={isLoading}
                className="w-full"
              >
                Sign in
              </Button>
            </div>
          </form>
          
          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">
                  Default credentials
                </span>
              </div>
            </div>
            
            <div className="mt-6 grid grid-cols-1 gap-3">
              <div className="bg-gray-50 rounded-md p-3 text-sm text-gray-700">
                <p><strong>Admin:</strong> username: admin, password: admin</p>
                <p className="mt-1"><strong>Manager/Employee:</strong> As created by admin</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
