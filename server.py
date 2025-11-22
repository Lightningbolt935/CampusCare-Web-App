from flask import Flask, request, jsonify
from flask_cors import CORS
from datetime import datetime
import sqlite3
import os

# Initialize Flask app
app = Flask(__name__)
CORS(app)

DATABASE = 'srm_complaints.db'

# ============================================
# DATABASE INITIALIZATION
# ============================================

def init_db():
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    
    # Users table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL,
            role TEXT NOT NULL,
            name TEXT
        )
    ''')
    
    # Complaints table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS complaints (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            faculty_id TEXT NOT NULL,
            faculty TEXT,
            category TEXT NOT NULL,
            type TEXT NOT NULL,
            floor TEXT NOT NULL,
            classroom TEXT NOT NULL,
            status TEXT DEFAULT 'Pending',
            description TEXT,
            image TEXT,
            assigned_incharge TEXT,
            worker TEXT,
            worker_job_type TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            on_hold_reason TEXT
        )
    ''')
    
    # Workers table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS workers (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            job_type TEXT
        )
    ''')
    
    # Insert sample users if table is empty
    cursor.execute('SELECT COUNT(*) FROM users')
    if cursor.fetchone()[0] == 0:
        sample_users = [
            ('faculty1', 'password123', 'faculty', 'Dr. John Smith'),
            ('faculty2', 'password123', 'faculty', 'Dr. Sarah Johnson'),
            ('incharge_electrical', 'password123', 'incharge', 'Incharge Electrical'),
            ('incharge_architecture', 'password123', 'incharge', 'Incharge Architecture'),
            ('incharge_plumbing', 'password123', 'incharge', 'Incharge Plumbing'),
            ('admin1', 'password123', 'admin', 'Admin User'),
            ('worker1', 'password123', 'worker', 'Rajesh Kumar'),
            ('worker2', 'password123', 'worker', 'Suresh Babu')
        ]
        cursor.executemany('INSERT INTO users (username, password, role, name) VALUES (?, ?, ?, ?)', sample_users)
    
    # Insert sample workers if table is empty
    cursor.execute('SELECT COUNT(*) FROM workers')
    if cursor.fetchone()[0] == 0:
        sample_workers = [
            ('Rajesh Kumar', 'Electrical'),
            ('Suresh Babu', 'Electrical'),
            ('Ramesh Sharma', 'Plumbing'),
            ('Mahesh Reddy', 'Plumbing'),
            ('Ganesh Patel', 'Architecture'),
            ('Dinesh Singh', 'Architecture')
        ]
        cursor.executemany('INSERT INTO workers (name, job_type) VALUES (?, ?)', sample_workers)
    
    conn.commit()
    conn.close()

def get_db():
    conn = sqlite3.connect(DATABASE)
    conn.row_factory = sqlite3.Row
    return conn

# ============================================
# AUTHENTICATION ROUTES
# ============================================

@app.route('/explore', methods=['POST'])
def login():
    data = request.json
    username = data.get('username')
    password = data.get('password')
    
    if not username or not password:
        return jsonify({'success': False, 'message': 'Username and password required'}), 400
    
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM users WHERE username = ? AND password = ?', (username, password))
    user = cursor.fetchone()
    conn.close()
    
    if user:
        return jsonify({
            'success': True,
            'message': 'Login successful',
            'user': {
                'id': user['id'],
                'username': user['username'],
                'role': user['role'],
                'name': user['name']
            }
        })
    else:
        return jsonify({'success': False, 'message': 'Invalid credentials'}), 401

# ============================================
# FACULTY ROUTES
# ============================================

@app.route('/faculty', methods=['POST'])
def submit_complaint():
    data = request.json
    
    print(f"\n[DEBUG] Faculty Complaint Submission:")
    print(f"  Received data: {data}")
    
    required_fields = ['faculty_id', 'category', 'type', 'floor', 'classroom', 'description']
    for field in required_fields:
        if field not in data:
            return jsonify({'success': False, 'message': f'Missing field: {field}'}), 400
    
    conn = get_db()
    cursor = conn.cursor()
    
    cursor.execute('SELECT name FROM users WHERE username = ? OR id = ?', 
                   (data['faculty_id'], data['faculty_id']))
    faculty = cursor.fetchone()
    faculty_name = faculty['name'] if faculty else data['faculty_id']
    
    assigned_incharge = data.get('assigned_incharge', '')
    
    print(f"  Assigned to incharge: '{assigned_incharge}'")
    
    cursor.execute('''
        INSERT INTO complaints 
        (faculty_id, faculty, category, type, floor, classroom, status, description, image, assigned_incharge)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ''', (
        data['faculty_id'],
        faculty_name,
        data['category'],
        data['type'],
        data['floor'],
        data['classroom'],
        data.get('status', 'Pending'),
        data['description'],
        data.get('image', ''),
        assigned_incharge
    ))
    
    conn.commit()
    complaint_id = cursor.lastrowid
    
    print(f"  Complaint #{complaint_id} created successfully")
    
    conn.close()
    
    return jsonify({
        'success': True,
        'message': 'Complaint submitted successfully',
        'complaint_id': complaint_id
    })

@app.route('/faculty/history/<faculty_id>', methods=['GET'])
def get_faculty_history(faculty_id):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute('''
        SELECT * FROM complaints 
        WHERE faculty_id = ? 
        ORDER BY created_at DESC
    ''', (faculty_id,))
    complaints = cursor.fetchall()
    conn.close()
    
    complaints_list = []
    for complaint in complaints:
        complaints_list.append({
            'id': complaint['id'],
            'category': complaint['category'],
            'type': complaint['type'],
            'floor': complaint['floor'],
            'classroom': complaint['classroom'],
            'status': complaint['status'],
            'description': complaint['description'],
            'image': complaint['image'],
            'assigned_incharge': complaint['assigned_incharge'],
            'worker': complaint['worker'],
            'created_at': complaint['created_at']
        })
    
    return jsonify({'complaints': complaints_list})

# ============================================
# INCHARGE ROUTES
# ============================================

@app.route('/complaints', methods=['GET'])
def get_complaints():
    incharge = request.args.get('incharge')
    
    print(f"\n[DEBUG] Incharge Dashboard Query:")
    print(f"  Filtering by incharge: '{incharge}'")
    
    conn = get_db()
    cursor = conn.cursor()
    
    if incharge:
        cursor.execute('''
            SELECT * FROM complaints 
            WHERE assigned_incharge = ? 
            ORDER BY created_at DESC
        ''', (incharge,))
    else:
        cursor.execute('SELECT * FROM complaints ORDER BY created_at DESC')
    
    complaints = cursor.fetchall()
    print(f"  Results found: {len(complaints)}")
    
    conn.close()
    
    complaints_list = []
    for complaint in complaints:
        complaints_list.append({
            'id': complaint['id'],
            'faculty_id': complaint['faculty_id'],
            'faculty': complaint['faculty'],
            'category': complaint['category'],
            'type': complaint['type'],
            'floor': complaint['floor'],
            'classroom': complaint['classroom'],
            'status': complaint['status'],
            'description': complaint['description'],
            'image': complaint['image'],
            'assigned_incharge': complaint['assigned_incharge'],
            'worker': complaint['worker'],
            'worker_job_type': complaint['worker_job_type'],
            'created_at': complaint['created_at'],
            'on_hold_reason': complaint['on_hold_reason']
        })
    
    return jsonify(complaints_list)

@app.route('/complaints/<int:complaint_id>', methods=['GET'])
def get_complaint(complaint_id):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM complaints WHERE id = ?', (complaint_id,))
    complaint = cursor.fetchone()
    conn.close()
    
    if complaint:
        return jsonify({
            'id': complaint['id'],
            'faculty_id': complaint['faculty_id'],
            'faculty': complaint['faculty'],
            'category': complaint['category'],
            'type': complaint['type'],
            'floor': complaint['floor'],
            'classroom': complaint['classroom'],
            'status': complaint['status'],
            'description': complaint['description'],
            'image': complaint['image'],
            'assigned_incharge': complaint['assigned_incharge'],
            'worker': complaint['worker'],
            'worker_job_type': complaint['worker_job_type'],
            'created_at': complaint['created_at'],
            'on_hold_reason': complaint['on_hold_reason']
        })
    else:
        return jsonify({'error': 'Complaint not found'}), 404

@app.route('/complaints/<int:complaint_id>/assign', methods=['PUT'])
def assign_worker(complaint_id):
    data = request.json
    
    if 'worker' not in data:
        return jsonify({'success': False, 'message': 'Worker name required'}), 400
    
    conn = get_db()
    cursor = conn.cursor()
    
    cursor.execute('SELECT * FROM complaints WHERE id = ?', (complaint_id,))
    if not cursor.fetchone():
        conn.close()
        return jsonify({'success': False, 'message': 'Complaint not found'}), 404
    
    cursor.execute('''
        UPDATE complaints 
        SET worker = ?, 
            worker_job_type = ?, 
            status = ?,
            on_hold_reason = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
    ''', (
        data['worker'],
        data.get('worker_job_type', ''),
        data.get('status', 'In Progress'),
        data.get('onHoldReason'),
        complaint_id
    ))
    
    conn.commit()
    conn.close()
    
    return jsonify({
        'success': True,
        'message': 'Worker assigned successfully'
    })

@app.route('/workers', methods=['GET'])
def get_workers():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM workers ORDER BY name')
    workers = cursor.fetchall()
    conn.close()
    
    workers_list = []
    for worker in workers:
        workers_list.append({
            'id': worker['id'],
            'name': worker['name'],
            'job_type': worker['job_type']
        })
    
    return jsonify(workers_list)

# ============================================
# WORKER ROUTES (NEW)
# ============================================

@app.route('/worker/complaints', methods=['GET'])
def get_worker_complaints():
    worker_name = request.args.get('worker')
    
    print(f"\n[DEBUG] Worker Dashboard Query:")
    print(f"  Filtering by worker: '{worker_name}'")
    
    if not worker_name:
        return jsonify({'error': 'Worker name required'}), 400
    
    conn = get_db()
    cursor = conn.cursor()
    
    cursor.execute('''
        SELECT * FROM complaints 
        WHERE worker = ? 
        ORDER BY 
            CASE status
                WHEN 'Pending' THEN 1
                WHEN 'In Progress' THEN 2
                WHEN 'On Hold' THEN 3
                WHEN 'Completed' THEN 4
            END,
            created_at DESC
    ''', (worker_name,))
    
    complaints = cursor.fetchall()
    print(f"  Results found: {len(complaints)}")
    
    conn.close()
    
    complaints_list = []
    for complaint in complaints:
        complaints_list.append({
            'id': complaint['id'],
            'faculty_id': complaint['faculty_id'],
            'faculty': complaint['faculty'],
            'category': complaint['category'],
            'type': complaint['type'],
            'floor': complaint['floor'],
            'classroom': complaint['classroom'],
            'status': complaint['status'],
            'description': complaint['description'],
            'image': complaint['image'],
            'assigned_incharge': complaint['assigned_incharge'],
            'worker': complaint['worker'],
            'worker_job_type': complaint['worker_job_type'],
            'created_at': complaint['created_at'],
            'updated_at': complaint['updated_at'],
            'on_hold_reason': complaint['on_hold_reason']
        })
    
    return jsonify(complaints_list)

@app.route('/complaints/<int:complaint_id>/status', methods=['PUT'])
def update_complaint_status(complaint_id):
    data = request.json
    
    print(f"\n[DEBUG] Status Update Request:")
    print(f"  Complaint ID: {complaint_id}")
    print(f"  New Status: {data.get('status')}")
    
    if 'status' not in data:
        return jsonify({'success': False, 'message': 'Status required'}), 400
    
    conn = get_db()
    cursor = conn.cursor()
    
    cursor.execute('SELECT * FROM complaints WHERE id = ?', (complaint_id,))
    complaint = cursor.fetchone()
    
    if not complaint:
        conn.close()
        return jsonify({'success': False, 'message': 'Complaint not found'}), 404
    
    new_status = data['status']
    on_hold_reason = data.get('onHoldReason', None)
    
    cursor.execute('''
        UPDATE complaints 
        SET status = ?,
            on_hold_reason = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
    ''', (new_status, on_hold_reason, complaint_id))
    
    conn.commit()
    
    print(f"  Status updated successfully")
    print(f"  Old Status: {complaint['status']} -> New Status: {new_status}")
    
    conn.close()
    
    return jsonify({
        'success': True,
        'message': 'Status updated successfully',
        'complaint_id': complaint_id,
        'new_status': new_status
    })

@app.route('/worker/stats/<worker_name>', methods=['GET'])
def get_worker_stats(worker_name):
    conn = get_db()
    cursor = conn.cursor()
    
    cursor.execute('''
        SELECT 
            status,
            COUNT(*) as count
        FROM complaints 
        WHERE worker = ?
        GROUP BY status
    ''', (worker_name,))
    
    stats = cursor.fetchall()
    conn.close()
    
    result = {
        'worker': worker_name,
        'total': 0,
        'pending': 0,
        'in_progress': 0,
        'completed': 0,
        'on_hold': 0
    }
    
    for stat in stats:
        status = stat['status']
        count = stat['count']
        result['total'] += count
        
        if status == 'Pending':
            result['pending'] = count
        elif status == 'In Progress':
            result['in_progress'] = count
        elif status == 'Completed':
            result['completed'] = count
        elif status == 'On Hold':
            result['on_hold'] = count
    
    return jsonify(result)

# ============================================
# UTILITY ROUTES
# ============================================

@app.route('/', methods=['GET'])
def index():
    return jsonify({
        'status': 'running',
        'message': 'SRM Complaints Management System API',
        'version': '1.0'
    })

# ============================================
# MAIN
# ============================================

if __name__ == '__main__':
    if not os.path.exists(DATABASE):
        print("Initializing database...")
        init_db()
        print("Database initialized successfully!")
    
    print("\n" + "="*50)
    print("SRM Complaints Management System - Backend Server")
    print("="*50)
    print("\nSample Login Credentials:")
    print("-" * 50)
    print("Faculty:")
    print("  Username: faculty1, Password: password123")
    print("  Username: faculty2, Password: password123")
    print("\nIncharge:")
    print("  Username: incharge_electrical, Password: password123")
    print("  Username: incharge_architecture, Password: password123")
    print("  Username: incharge_plumbing, Password: password123")
    print("\nAdmin:")
    print("  Username: admin1, Password: password123")
    print("\nWorker:")
    print("  Username: worker1, Password: password123 (Rajesh Kumar)")
    print("  Username: worker2, Password: password123 (Suresh Babu)")
    print("-" * 50)
    print("\nServer starting on http://0.0.0.0:5000")
    print("Press CTRL+C to stop the server\n")
    
    app.run(host='0.0.0.0', port=5000, debug=True)