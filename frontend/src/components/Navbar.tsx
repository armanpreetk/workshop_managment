import React from 'react';
import Button from './Button';

export default function Navbar({ user, onLogout }: { user: any; onLogout: () => void }) {
  return (
    <nav className="bg-white shadow">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex">
            <div className="flex-shrink-0 flex items-center">
              <span className="text-xl font-bold text-blue-600">Workshop Management</span>
            </div>
          </div>
          <div className="flex items-center">
            <div className="hidden md:ml-4 md:flex-shrink-0 md:flex md:items-center">
              <div className="ml-3 relative flex items-center space-x-4">
                <div className="text-sm font-medium text-gray-700">
                  {user?.name} ({user?.type})
                </div>
                <Button 
                  variant="secondary" 
                  size="sm" 
                  onClick={onLogout}
                >
                  Logout
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
}